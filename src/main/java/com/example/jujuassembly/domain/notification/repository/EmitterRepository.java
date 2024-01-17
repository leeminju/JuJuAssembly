package com.example.jujuassembly.domain.notification.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// SSE를 위한 emitter 객체와 관련된 이벤트 데이터를 관리하는 클래스
// 'Map'을 사용하여 인스턴스 이벤트 데이터를 메모리 내 관리하는 특정한 방식의 데이터 관리를 구현
@Repository
public class EmitterRepository {

  // ID-KEY / SseEmitter객체-value, SSE 연결을 저장하고 관리
  public final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  // 이벤트 데이터를 캐싱하는 데 사용 / 각 이벤트에는 고유한 ID 존재
  private final Map<String, Object> eventCache = new ConcurrentHashMap<>();


  // SseEmitter 객체 저장
  public SseEmitter save(String id, SseEmitter sseEmitter) {
    emitters.put(id, sseEmitter);
    return sseEmitter;
  }

  // 이벤트 데이터를 캐시에 저장
  public void saveEventCache(String id, Object event) {
    eventCache.put(id, event);
  }

  // 주어진 ID로 시작하는 모든 SseEmitter 객체를 찾아 반환
  public Map<String, SseEmitter> findAllStartWithById(String id) {
    return emitters.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(id))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  // 주어진 ID로 시작하는 모든 이벤트 데이터를 찾아 반환
  public Map<String, Object> findAllEventCacheStartWithId(String id) {
    return eventCache.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(id))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  // 주어진 ID로 시작하는 모든 SseEmitter 객체 삭제
  public void deleteAllStartWithId(String id) {
    emitters.forEach(
        (key, emitter) -> {
          if (key.startsWith(id)) {
            emitters.remove(key);
          }
        }
    );
  }

  // 특정 ID를 가진 SseEmitter 객체 삭제
  public void deleteById(String id) {
    emitters.remove(id);
  }


  // 주어진 ID로 시작하는 모든 이벤트 데이터를 캐시에서 삭제
  public void deleteAllEventCacheStartWithId(String id) {
    eventCache.forEach(
        (key, data) -> {
          if (key.startsWith(id)) {
            eventCache.remove(key);
          }
        }
    );
  }

}
