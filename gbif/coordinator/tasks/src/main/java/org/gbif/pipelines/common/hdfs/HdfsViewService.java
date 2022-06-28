package org.gbif.pipelines.common.hdfs;

import com.google.common.util.concurrent.AbstractIdleService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.gbif.common.messaging.DefaultMessagePublisher;
import org.gbif.common.messaging.MessageListener;
import org.gbif.common.messaging.api.MessagePublisher;
import org.gbif.common.messaging.api.messages.PipelineBasedMessage;
import org.gbif.common.messaging.api.messages.PipelinesInterpretationMessage;
import org.gbif.pipelines.common.configs.StepConfiguration;
import org.gbif.pipelines.tasks.ServiceFactory;
import org.gbif.registry.ws.client.pipelines.PipelinesHistoryClient;

/** A service which listens to the {@link PipelinesInterpretationMessage } */
@Slf4j
public class HdfsViewService<
        I extends PipelinesInterpretationMessage, B extends PipelineBasedMessage>
    extends AbstractIdleService {

  private final org.gbif.pipelines.common.hdfs.HdfsViewConfiguration config;
  private final HdfsCallbackFactory<I, B> callbackFactory;
  private MessageListener listener;
  private MessagePublisher publisher;
  private CuratorFramework curator;
  private ExecutorService executor;

  public HdfsViewService(HdfsViewConfiguration config, HdfsCallbackFactory<I, B> callbackFactory) {
    this.config = config;
    this.callbackFactory = callbackFactory;
  }

  @Override
  protected void startUp() throws Exception {
    log.info(
        "Started pipelines-{}-hdfs-view service with parameters : {}", config.stepType, config);
    // Prefetch is one, since this is a long-running process.
    StepConfiguration c = config.stepConfig;
    listener = new MessageListener(c.messaging.getConnectionParameters(), 1);
    publisher = new DefaultMessagePublisher(c.messaging.getConnectionParameters());
    curator = c.zooKeeper.getCuratorFramework();
    executor =
        config.standaloneNumberThreads == null
            ? null
            : Executors.newFixedThreadPool(config.standaloneNumberThreads);

    PipelinesHistoryClient historyClient =
        ServiceFactory.createPipelinesHistoryClient(config.stepConfig);

    HdfsViewCallback<I, B> callback =
        callbackFactory.createCallBack(config, publisher, curator, historyClient, executor);

    listener.listen(c.queueName, callback.routingKey(), c.poolSize, callback);
  }

  @Override
  protected void shutDown() {
    listener.close();
    publisher.close();
    curator.close();
    executor.shutdown();
    log.info("Stopping pipelines-" + config.recordType.name().toLowerCase() + "-hdfs-view service");
  }
}
