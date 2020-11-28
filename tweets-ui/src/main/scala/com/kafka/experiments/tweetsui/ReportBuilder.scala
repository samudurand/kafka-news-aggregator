package com.kafka.experiments.tweetsui

import io.rocketbase.mail.EmailTemplateBuilder
import io.rocketbase.mail.model.HtmlTextEmail
import io.rocketbase.mail.model.HtmlTextEmail.HtmlTextEmailBuilder

object ReportBuilder {

  def generateReport(): HtmlTextEmail = {
    import io.rocketbase.mail.config.TbConfiguration
    import io.rocketbase.mail.model.HtmlTextEmail
    import java.math.BigDecimal
    val config = TbConfiguration.newInstance
    config.getContent.setFull(true)

    val builder = new EmailTemplateBuilder.EmailTemplateConfigBuilder()
    builder
      .configuration(config)
      .header
      .text("Hello")
      .and
      .text("Hi {{name}},")
      .and
      .text("Thanks for using [Product Name]. This is an invoice for your recent purchase")
      .and
      .tableSimple("#.## '€'")
      .headerRow("Description", "Amount")
      .itemRow("Special Product\n" + "Some extra explanations in separate line", BigDecimal.valueOf(1333, 2))
      .itemRow("Short service", BigDecimal.valueOf(103, 1))
      .footerRow("Total", BigDecimal.valueOf(2363, 2))
      .and
      .button("Download PDF", "http://localhost")
      .gray
      .right
      .and
      .text(
        "If you have any questions about this receipt, simply reply to this email or reach out to our support team for help."
      )
      .and
      .copyright("rocketbase")
      .url("https://www.rocketbase.io")
      .suffix(". All rights reserved.")
      .and
      .footerText("[Company Name, LLC]\n" + "1234 Street Rd.\n" + "Suite 1234")
      .and
      .build()
  }

}
