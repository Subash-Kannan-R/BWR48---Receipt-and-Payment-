-- =====================================================================
-- Run this against a NEW schema named `result` before starting the app.
-- Table names/columns match the ORIGINAL legacy JSP's reptdt and
-- dv_locreptandpayment tables exactly, so anything downstream that
-- already reads from these table/column names keeps working.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS result;
USE result;

-- One row per "Generate Consolidation" click.
CREATE TABLE reptdt (
    recordno        INT PRIMARY KEY,
    rtype           VARCHAR(20),
    reptdate        DATE,
    reptgendate     DATE,
    backupdt        DATE,
    progconsrno     VARCHAR(20),
    progconsrname   VARCHAR(150),
    finyear         VARCHAR(20),
    fromdt          DATE,
    ywcomp          VARCHAR(1),
    chartfinal      VARCHAR(1),
    finyearcode     VARCHAR(10),
    brno            VARCHAR(20)
);

-- One row per account-by-account amount, per location, per side (R/P),
-- plus a Total row per location per side (reptahcode='9998' / payahcode='9999').
-- ctxt/pcode/rcode/c1-c6/ufc/dtcode/dtcodeslno/stcode are written as blank
-- strings - those depended on the legacy "feddet" table, which this app no
-- longer uses. fedname/blkname/rname/stname/dtname are populated from the
-- federation/region/district/state lookups this app does have.
CREATE TABLE dv_locreptandpayment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    fedname         VARCHAR(100),
    repttype        VARCHAR(5),
    reptahcode      VARCHAR(10),
    reptahname      VARCHAR(150),
    reptamt         DECIMAL(15,2),
    repttotamt      DECIMAL(15,2),
    paytype         VARCHAR(5),
    payahcode       VARCHAR(10),
    payahname       VARCHAR(150),
    payamt          DECIMAL(15,2),
    paytotamt       DECIMAL(15,2),
    reptdate        DATE,
    co              INT,
    ctxt            VARCHAR(5),
    stname          VARCHAR(100),
    dtname          VARCHAR(100),
    lvl             INT,
    reptno          INT,
    reptgendate     DATE,
    pname           VARCHAR(50),
    rname           VARCHAR(100),
    pcode           VARCHAR(10),
    rcode           VARCHAR(10),
    c1              VARCHAR(100),
    c2              VARCHAR(100),
    c3              VARCHAR(100),
    c4              VARCHAR(100),
    c5              VARCHAR(100),
    c6              VARCHAR(100),
    ufc             VARCHAR(20),
    rtype           VARCHAR(20),
    backupdt        DATE,
    progconsrno     VARCHAR(20),
    progconsrname   VARCHAR(150),
    brno            VARCHAR(20),
    blkname         VARCHAR(100),
    dtcode          VARCHAR(10),
    dtcodeslno      VARCHAR(10),
    stcode          VARCHAR(10),
    finyear         VARCHAR(20),
    fromdt          DATE,
    ywcomp          VARCHAR(1),
    chartfinal      VARCHAR(1),
    finyearcode     VARCHAR(10),
    vc70            VARCHAR(20)
);
