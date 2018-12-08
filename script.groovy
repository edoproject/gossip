import org.fluentd.logger.FluentLogger
import com.gossip.Analyzer

String jenkins_id = env.get("JENKINS_ID", "UNKNOWN_JENKINS_ID")
String fluentd_ip = env.get("FLUENTD_IP", "127.0.0.1")

FluentLogger logger = FluentLogger.getLogger(jenkins_id, fluentd_ip, 24224)
Analyzer analyzer = new Analyzer(logger, jenkins_id)

if (run instanceof hudson.model.Run){
    analyzer.processEvent(new com.gossip.RunImpl(run), event)
}
