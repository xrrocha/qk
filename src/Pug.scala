package pk

import de.neuland.pug4j.PugConfiguration
import de.neuland.pug4j.expression.GraalJsExpressionHandler
import de.neuland.pug4j.template.ReaderTemplateLoader
import java.io.StringReader
import org.graalvm.polyglot.*
import scala.jdk.CollectionConverters.*

object Pug:
    def render(
        templateText: String,
        templateName: String,
        model: Map[String, Any | Null]
    ): String =
        val config = PugConfiguration()
        config.setExpressionHandler(GraalJsExpressionHandler())

        val templateLoader = ReaderTemplateLoader(
          StringReader(templateText),
          templateName
        )
        config.setTemplateLoader(templateLoader)

        val template = config.getTemplate(templateName)

        config.renderTemplate(template, model.asJava)
end Pug
