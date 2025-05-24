#!/bin/sh
set -e
makeschema(){
  ts-json-schema-generator -p types.d.ts -t $1 -o $2
}
makeschema Skill build/skill.json
makeschema LockGroup build/lock_group.json
