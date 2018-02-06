package nl.knaw.dans.easy.validatebag.lib

trait Rules {

  def rules: Map[ProfileVersion, RuleBase]
}
