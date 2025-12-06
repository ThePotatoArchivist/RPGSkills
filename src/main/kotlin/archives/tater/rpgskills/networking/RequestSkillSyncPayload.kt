package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills

object RequestSkillSyncPayload : SingletonPayload<RequestSkillSyncPayload>(RPGSkills.id("request_skill_sync"))
