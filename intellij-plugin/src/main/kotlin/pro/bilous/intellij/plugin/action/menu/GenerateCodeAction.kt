package pro.bilous.intellij.plugin.action.menu

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.showOkCancelDialog
import org.slf4j.LoggerFactory
import pro.bilous.intellij.plugin.gen.CodeGenerator

class GenerateCodeAction : AnAction() {

	companion object {
		private val log = LoggerFactory.getLogger(GenerateCodeAction::class.java)
	}

    override fun actionPerformed(e: AnActionEvent) {
		val ve = VerifiedEvent(e)
		try {
			CodeGenerator().generate(ve.projectPath)
		} catch (e: Exception) {
			log.error("ERROR forward: " + e.message)
			showOkCancelDialog(
				title = "Code Generation Failed",
				message = e.message ?: "No message",
				okText = "Okay"
			)
		}
    }
}
