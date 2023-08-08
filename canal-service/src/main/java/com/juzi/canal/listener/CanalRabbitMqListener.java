package com.juzi.canal.listener;

import com.google.gson.Gson;
import com.juzi.canal.pojo.CanalMessage;
import com.juzi.canal.utils.RedisCommonProcessor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author codejuzi
 */
@Component
public class CanalRabbitMqListener {
    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "canal.queue", durable = "true"),
                    exchange = @Exchange(value = "canal.exchange"),
                    key = "canal.routing.key"
            )
    })
    public void handleDataChange(String message) {
        Gson gson = new Gson();
        try {
            CanalMessage<?> msg = gson.fromJson(message, CanalMessage.class);
            if ("user".equals(msg.getTable()) && "oauth".equals(msg.getDatabase()) && !"INSERT".equals(msg.getType())) {
                List<Map<String, Object>> dataList = msg.getData();
                for (Map<String, Object> data : dataList) {
                    String userId = String.valueOf(data.get("id"));
                    if (userId != null) {
                        String personId = String.valueOf(Integer.parseInt(userId) + 10000000);
                        redisCommonProcessor.remove(personId);
                    }
                }
            }
        } catch (Exception e) {
            // Handle the exception and retry processing the message if necessary.
        }
    }

}
