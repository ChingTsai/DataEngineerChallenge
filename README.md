# Session Analysis
- Build jar
  ```bash
  make build
  ```
- Execute jar
  ```bash
  make deploy_build run THRESHOLD=600 OUTPUT_LIMIT=100
  ```
- Execute prebuild jar
  ```bash
  make deploy_prebuild run THRESHOLD=600 OUTPUT_LIMIT=100
  ```
- Clean
  ```bash
  make clean
  ```

## Result
  - The Output is located at `out` folder in JSON lines.
    - Each row represents one session
    - Sorted by `sessionTime` desc
    - Limit 100 rows
  - A single visitor is identified by 
    - IP address
    - Device name
    - Agent name version
    - Major operating system name version
    - Agent class
  - Using 10 minutes session window time to sessionize events created by each visitor.
    - A serial `sessionId` is assigned to each session.
  - `hits` represents unique URL visits per session.
  - `avgSessionTime` represents the average sesstion time of sessions that is not 0-length.
