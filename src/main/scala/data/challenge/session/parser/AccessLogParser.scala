package data.challenge.session.parser

case class Event(
                  timestamp: String = null,
                  elb_name: String = null,
                  request_ip: String = null,
                  request_port: String = null,
                  backend_ip: String = null,
                  backend_port: String = null,
                  request_processing_time: String = null,
                  backend_processing_time: String = null,
                  client_response_time: String = null,
                  elb_response_code: String = null,
                  backend_response_code: String = null,
                  received_bytes: String = null,
                  sent_bytes: String = null,
                  request_verb: String = null,
                  url: String = null,
                  protocol: String = null,
                  user_agent: Map[String, String] = null,
                  ssl_cipher: String = null,
                  ssl_protocol: String = null,
                  _corrupt_record: String = null
                )

object AccessLogParser {
  // ref: https://docs.aws.amazon.com/athena/latest/ug/elasticloadbalancer-classic-logs.html
  val pattern = """([^ ]*) ([^ ]*) ([^ ]*):([0-9]*) ([^ ]*)[:-]([0-9]*) ([-.0-9]*) ([-.0-9]*) ([-.0-9]*) (|[-0-9]*) (-|[-0-9]*) ([-0-9]*) ([-0-9]*) \"([^ ]*) ([^ ]*) (- |[^ ]*)\" (\"[^\"]*\") ([A-Z0-9-]+) ([A-Za-z0-9.-]*)$""".r

  def parseAccessLog(log: String)(implicit setting: UserAgentParserSetting): Event = {
    log match {
      case pattern(timestamp, elb_name, request_ip, request_port, backend_ip, backend_port, request_processing_time, backend_processing_time, client_response_time, elb_response_code, backend_response_code, received_bytes, sent_bytes, request_verb, url, protocol, user_agent, ssl_cipher, ssl_protocol) =>
        Event(
          timestamp,
          elb_name,
          request_ip,
          request_port,
          backend_ip,
          backend_port,
          request_processing_time,
          backend_processing_time,
          client_response_time,
          elb_response_code,
          backend_response_code,
          received_bytes,
          sent_bytes,
          request_verb,
          url,
          protocol,
          UserAgentParser.parseUserAgent(user_agent),
          ssl_cipher,
          ssl_protocol
        )
      case _ =>
        Event(_corrupt_record = log)
    }
  }

}
