package com.example.project.messaging;

import com.example.project.dtos.QueueBMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

import static com.example.project.service.YouTubeApiService.readFileFromClasspath;

@Component
@Slf4j
@RequiredArgsConstructor
public class Receiver {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "QueueA", containerFactory = "myFactory")
    public void receiveMessage(String queueAMessage) throws IOException {
        log.info("Received <" + queueAMessage + ">");
        QueueBMessage queueBMessage = transformMessage(queueAMessage);
        sendQueueBMessage("QueueB", queueBMessage);
    }


    private QueueBMessage transformMessage(final String message) {
        QueueBMessage queueBMessage = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(QueueBMessage.class);
            Unmarshaller u = jc.createUnmarshaller();
            StreamSource streamSource = new StreamSource(new StringReader(message));
            JAXBElement<QueueBMessage> responseObj = u.unmarshal(streamSource, QueueBMessage.class);
            queueBMessage = responseObj.getValue();
            queueBMessage = queueBMessage.toBuilder()
                    .videoTitle(queueBMessage.getVideoTitle().replaceAll("(?i)telecom", "telco"))
                    .build();
            log.info(
                    "transformMessage for queue B {}", queueBMessage);

        } catch (JAXBException e) {
            log.error("error in transformMessage:{}", e.getMessage(), e);
        }
        return queueBMessage;
    }

    private void sendQueueBMessage(String queueName, QueueBMessage queueBMessage) throws IOException {
        String xmlQueueBMessage = readFileFromClasspath("/templates/QueueBMessage.xml");

        xmlQueueBMessage = xmlQueueBMessage.replace("{url}", queueBMessage.getUrl());
        xmlQueueBMessage = xmlQueueBMessage.replace("{videoTitle}", queueBMessage.getVideoTitle());
        log.info("Sending the item to QueueB {}", xmlQueueBMessage);
        jmsTemplate.convertAndSend(queueName, xmlQueueBMessage);
    }
}