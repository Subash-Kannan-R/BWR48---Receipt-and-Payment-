-- =====================================================================
-- Tables this app reads from, all in ONE schema (2526KFUrbanSellur_FA).
-- Column list and types are inferred from every query ReportRepository.java
-- runs. Adjust lengths/precision to match your real conventions if you
-- already have these tables elsewhere with different definitions -
-- this is a best-effort reconstruction from the legacy JSP's SQL, not
-- your actual DDL (which I don't have).
-- =====================================================================

-- ---------------------------------------------------------------------
-- federation
-- Used for: federation display name (fedname/blkname) and fedcode.
--   select fedname, fedcode from federation where fedname != '' and brno = ?
--   union select blkname, fedcode from federation where (fedname is null or fedname = '') and brno = ?
--
-- rcode/dtcode/stcode are ReportRepository's best-effort guess for where
-- region/district/state codes live now that the legacy "feddet" table is
-- no longer used. If your federation table doesn't have these three
-- columns, drop them here and the app will just leave region/district/
-- state blank in the response (it degrades gracefully, doesn't error).
-- ---------------------------------------------------------------------
CREATE TABLE federation (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    fedcode  VARCHAR(10)  NOT NULL,
    fedname  VARCHAR(100),
    blkname  VARCHAR(100),
    brno     VARCHAR(20)  NOT NULL,
    rcode    VARCHAR(10),
    dtcode   VARCHAR(10),
    stcode   VARCHAR(10),
    KEY idx_federation_brno (brno),
    KEY idx_federation_fedcode (fedcode)
);

-- ---------------------------------------------------------------------
-- accthead
-- Account head master: code -> display name, scoped per branch.
--   select ahcode, ahname from accthead where brno = ? order by ahcode
-- ---------------------------------------------------------------------
CREATE TABLE accthead (
    ahcode   VARCHAR(10)  NOT NULL,
    ahname   VARCHAR(150) NOT NULL,
    brno     VARCHAR(20)  NOT NULL,
    PRIMARY KEY (ahcode, brno)
);

-- ---------------------------------------------------------------------
-- grpdetail
-- One row per self-help group; joined against grpbalsheet/rcpttran/
-- vouchtran to filter by branch and by regular-vs-defunct group status.
--   ... a.grpcode = b.grpcode and b.removed != 'Y' and a.brno = b.brno
--       and a.brno = ? [and b.defGrp = 'N' | 'Y']
-- ---------------------------------------------------------------------
CREATE TABLE grpdetail (
    grpcode  VARCHAR(20) NOT NULL,
    brno     VARCHAR(20) NOT NULL,
    defGrp   CHAR(1)     NOT NULL DEFAULT 'N',   -- 'Y' = defunct group, 'N' = regular group
    removed  CHAR(1)     NOT NULL DEFAULT 'N',   -- 'Y' = soft-deleted, excluded from all totals
    PRIMARY KEY (grpcode, brno),
    KEY idx_grpdetail_brno (brno)
);

-- ---------------------------------------------------------------------
-- grpbalsheet
-- Opening balances per group per account head. Only ahCode 2111 (cash)
-- and 2112 (bank) are read by this report.
--   select sum(amt) from grpbalsheet a, grpdetail b
--   where a.grpcode = b.grpcode and b.removed != 'Y' and a.brno = b.brno
--   and a.brno = ? and a.ahCode = '2111' / '2112'
-- ---------------------------------------------------------------------
CREATE TABLE grpbalsheet (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    grpcode  VARCHAR(20)    NOT NULL,
    brno     VARCHAR(20)    NOT NULL,
    ahCode   VARCHAR(10)    NOT NULL,
    amt      DECIMAL(15,2)  NOT NULL DEFAULT 0,
    KEY idx_grpbalsheet_grp_brno (grpcode, brno),
    KEY idx_grpbalsheet_ahcode (ahCode)
);

-- ---------------------------------------------------------------------
-- rcpttran (receipts). Aliased "a" (unfiltered join) or "b" (as-of-date
-- cutoff join) in ReportRepository - same table either way.
--   select sum(amt) from rcpttran b, grpdetail c
--   where b.grpcode = c.grpcode and c.removed != 'Y' and b.brno = c.brno
--   and b.brno = ? and rtype = 2 and rcptdt < ?
--
--   select sum(amt)/ahcode, sum(amt) from rcpttran a, grpdetail b
--   where a.grpcode = b.grpcode and b.removed != 'Y' and a.brno = b.brno
--   and a.brno = ? [and b.defGrp = ..] and month <= ? [and ahcode = '2112' | != '2112']
-- ---------------------------------------------------------------------
CREATE TABLE rcpttran (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    grpcode  VARCHAR(20)    NOT NULL,
    brno     VARCHAR(20)    NOT NULL,
    ahcode   VARCHAR(10)    NOT NULL,
    amt      DECIMAL(15,2)  NOT NULL DEFAULT 0,
    rtype    TINYINT        NOT NULL,   -- 2 = the type this report sums for the pre-period cash figure
    rcptdt   DATE           NOT NULL,
    month    TINYINT        NOT NULL,   -- 1-12, accounting month (independent of rcptdt's calendar month)
    KEY idx_rcpttran_grp_brno (grpcode, brno),
    KEY idx_rcpttran_ahcode (ahcode),
    KEY idx_rcpttran_month (month)
);

-- ---------------------------------------------------------------------
-- vouchtran (payments). Aliased "a" or "b" depending on the query, same
-- table as rcpttran's counterpart on the payments side.
--   select sum(amt) from vouchtran b, grpdetail c
--   where b.grpcode = c.grpcode and c.removed != 'Y' and b.brno = c.brno
--   and b.brno = ? and vtype = 2 and vouchdt < ?
--
--   select ahcode, sum(amt) from vouchtran a, grpdetail b
--   where a.grpcode = b.grpcode and b.removed != 'Y' and a.brno = b.brno
--   and a.brno = ? [and b.defGrp = ..] and month <= ? [and ahcode = '2112' | != '2112']
-- ---------------------------------------------------------------------
CREATE TABLE vouchtran (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    grpcode  VARCHAR(20)    NOT NULL,
    brno     VARCHAR(20)    NOT NULL,
    ahcode   VARCHAR(10)    NOT NULL,
    amt      DECIMAL(15,2)  NOT NULL DEFAULT 0,
    vtype    TINYINT        NOT NULL,   -- 2 = the type this report sums for the pre-period cash figure
    vouchdt  DATE           NOT NULL,
    month    TINYINT        NOT NULL,
    KEY idx_vouchtran_grp_brno (grpcode, brno),
    KEY idx_vouchtran_ahcode (ahcode),
    KEY idx_vouchtran_month (month)
);

-- ---------------------------------------------------------------------
-- region / district / state
-- Descriptive lookups only - the report works fine without them (the
-- Java code catches lookup failures and just leaves these blank).
--   select rname from region where rcode = ? and removed != 'Y'
--   select distname from district where dtcode = ?
--   select stname from state where stcode = ?
-- ---------------------------------------------------------------------
CREATE TABLE region (
    rcode    VARCHAR(10) NOT NULL PRIMARY KEY,
    rname    VARCHAR(100),
    shrname  VARCHAR(50),
    removed  CHAR(1) NOT NULL DEFAULT 'N'
);

CREATE TABLE district (
    dtcode    VARCHAR(10) NOT NULL PRIMARY KEY,
    distname  VARCHAR(100),
    shdtname  VARCHAR(50)
);

CREATE TABLE state (
    stcode    VARCHAR(10) NOT NULL PRIMARY KEY,
    stname    VARCHAR(100),
    shstname  VARCHAR(50)
);
