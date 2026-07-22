# Dhanam Consolidation — multi-schema report, results written to reptdt / dv_locreptandpayment

- **Backend**: Java 8, Spring Boot 2.7, `mysql-connector-java:5.1.49`, plain `JdbcTemplate`
- **Frontend**: React + TypeScript + Vite + Tailwind

## Architecture

Two kinds of database connections:

1. **Location schemas** (read-only source data) — every schema listed in
   `dhanam.location.schemas` (application.properties) gets queried, one
   column per schema. Currently:
   `2526KFUrbanSellur_FA, 2526KFDindigulShanarpatty_FA, 2526KFSolapurUrban, 2526KFUrbanArapalayam_FA`.
   Same tables in each: `federation`, `accthead`, `grpbalsheet`, `grpdetail`,
   `rcpttran`, `vouchtran`, `region`, `district`, `state`.

2. **`result` database** (write-only sink) — every "Generate Consolidation"
   click writes:
   - one **`reptdt`** row (report run summary — same columns as the legacy JSP)
   - one **`dv_locreptandpayment`** row per non-zero account amount, per
     location, per side (receipts/payments), plus a Total row per location
     per side — same table/column structure as the legacy JSP's inserts.

   Create this database first:
   ```
   mysql -u root -p < result-schema.sql
   ```

   Columns that depended on the removed `feddet` table (`ufc`, `pcode`,
   `rcode`, `dtcode`, `dtcodeslno`, `stcode`, `ctxt`, `c1`-`c6`) are written
   as blank strings — there's no equivalent source for them anymore.
   `fedname`/`blkname`/`rname`/`stname`/`dtname` are populated from the
   federation/region/district/state lookups this app does have.

## Run it

**Backend**
```
cd backend
# edit src/main/resources/application.properties:
#   dhanam.location.* credentials (source schemas)
#   dhanam.results.*  credentials (the `result` database)
mvn spring-boot:run
```

**Frontend**
```
cd frontend
npm install
npm run dev
```
Set `VITE_API_BASE_URL` in `frontend/.env` if your backend isn't on `localhost:8082`.

## Request fields

`POST /api/dhanam/consolidation/generate`:

`reportDate`, `backupDate`, `month`, `branchCategory`, `branchNo`, `reportType`,
`groupType`, `expectedAccountingYear` (as before), plus **back again**:
`finYear`, `finYearCode`, `finYearStartDate` — needed because `reptdt` and
`dv_locreptandpayment` require them, same as the original JSP's session
values (`REGFYFY`, `REGFYFYCODE`, `RFDATE`).

## Response

`resultsRunId` — the `reptdt.recordno` this generation was saved under
(`null` if the `result` database was unreachable; the report itself still
returns successfully either way).
`resultsLinesStored` — how many `dv_locreptandpayment` rows were written
(zero-amount rows are skipped, matching the original JSP's `if (amt <= 0) continue`).

## Display

Table layout is back to the **row model**: account heads (S.L. Code +
Particulars) run down the left, one column per location — matching your
BWR48 reference image, now with the banner/metadata strip kept from the
last round.

## Resilience

- One bad/unreachable location schema is skipped, not fatal — the report
  continues with whatever schemas succeeded.
- If the `result` database is unreachable, report computation/response are
  unaffected; only storage is skipped, shown as an amber notice in the UI.

## Still open

- `reportForLabel` is hardcoded to `"Kalanjiam/AFGs"`.
- The "Consolidation Baseline" / "Receipt and Payment" toggle is still UI-only.
- No connection pooling yet (plain `DriverManagerDataSource`).
- Auth — add whatever your app already uses in front of `/api/dhanam/**`.
