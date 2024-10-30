package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class Main1 {
  public static void main(String[] args) {
    try (var jedis = new Jedis()) {
      System.out.println("Subscriber to Ch-1 started");
      jedis.subscribe(new JedisPubSub() {
        @Override
        public void onMessage(String channel, String message) {
          System.out.println("channel = " + channel);
          System.out.println("message = " + message);
          sendMessage(channel, message);
        }
      }, "Ch-1");
    }


  }

  public static void sendMessage(String ch, String message) {
    try (var jedis = new Jedis()) {
      jedis.publish(ch, message);
    }
  }
}
