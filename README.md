## Instructions
### Setup
1. Copy .env.example to .env
2. Fill in the variables

### Usage
#### Dev
> clojure -M -m print-sprint.core --from 2023-09-26 --to 2023-10-10 --sprints 5 --ids xxxxx,xxxxx,xxxxx --board-id X
```
SageHR Report
| :employee_id | :days |       :name |
|--------------+-------+-------------|
|      xxxxxxx |     3 |  John Doe   |
|      xxxxxxx |     6 |  Mario      |
|      xxxxxxx |     2 |  Terminator |

Jira Report
| :sprint-id | :estimated | :completed |
|------------+------------+------------|
|       :123 |       22.0 |       80.0 |
|       :124 |       87.0 |       78.5 |
|       :125 |       57.0 |       65.5 |
|       :126 |       63.5 |       65.0 |
|       :127 |       72.5 |       55.5 |
Suggested story points for next sprint: 39
```
#### Binary
> ./print_sprint --from 2023-09-26 --to 2023-10-10 --sprints 5 --ids xxxxx,xxxxx,xxxxx --board-id X
