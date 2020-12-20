package data.challenge.session.parser

import nl.basjes.parse.useragent.UserAgentAnalyzer

case class UserAgentParserSetting(cacheSize: Int, parserFields: Seq[String])

object UserAgentParser {
  private var _uaa: UserAgentAnalyzer = _

  def getParser(implicit setting: UserAgentParserSetting): UserAgentAnalyzer = synchronized {
    // Only initial parser once per executor
    if (_uaa == null) {
      _uaa = UserAgentAnalyzer.newBuilder
        .withCache(setting.cacheSize)
        .withFields(setting.parserFields: _*)
        .build
    }
    _uaa
  }

  def parseUserAgent(ua: String)(implicit setting: UserAgentParserSetting): Map[String, String] = {
    import collection.JavaConverters._
    UserAgentParser.getParser.parse(ua).toMap().asScala.toMap
  }

}

