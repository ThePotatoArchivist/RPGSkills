package archives.tater.rpgskills.client

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.toast.JobMenuToast
import archives.tater.rpgskills.client.gui.toast.SkillMenuToast
import archives.tater.rpgskills.util.CodecConfig
import archives.tater.rpgskills.util.MutationCodec
import archives.tater.rpgskills.util.forAccess
import archives.tater.rpgskills.util.recordMutationCodec
import com.mojang.serialization.Codec

data class RPGSkillsClientState(
    var skillMenuHint: SkillMenuToast = SkillMenuToast(),
    var jobMenuHint: JobMenuToast = JobMenuToast(),
) {
    fun write() {
        write(this)
    }

    companion object : CodecConfig<RPGSkillsClientState>("rpgskills_client_state", RPGSkills.logger) {
        override val codec: MutationCodec<RPGSkillsClientState> = recordMutationCodec(
            SkillMenuToast.CODEC.fieldOf("seen_skill_menu_hint").forAccess(RPGSkillsClientState::skillMenuHint),
            JobMenuToast.CODEC.fieldOf("seen_job_menu_hint").forAccess(RPGSkillsClientState::jobMenuHint),
        )

        override fun getDefault() = RPGSkillsClientState()
    }
}