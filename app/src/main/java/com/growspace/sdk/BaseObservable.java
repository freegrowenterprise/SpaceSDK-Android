/**
 * BaseObservable 클래스는 옵저버 패턴을 구현하기 위한 기본 추상 클래스입니다.
 * 이 클래스는 리스너의 등록, 해제, 그리고 리스너 목록 관리를 담당합니다.
 * 
 * @param <LISTENER_CLASS> 리스너 인터페이스의 타입
 */
package com.growspace.sdk;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseObservable<LISTENER_CLASS> {
    // 동시성 처리를 위한 스레드 세이프한 리스너 컬렉션
    // ConcurrentHashMap을 사용하여 스레드 안전성 보장
    private final Set<LISTENER_CLASS> mListeners = Collections.newSetFromMap(new ConcurrentHashMap<>(1));

    /**
     * 새로운 리스너를 등록하는 메서드
     * @param listener_class 등록할 리스너 객체
     */
    public final void registerListener(LISTENER_CLASS listener_class) {
        this.mListeners.add(listener_class);
    }

    /**
     * 등록된 리스너를 해제하는 메서드
     * @param listener_class 해제할 리스너 객체
     */
    public final void unregisterListener(LISTENER_CLASS listener_class) {
        this.mListeners.remove(listener_class);
    }

    /**
     * 현재 등록된 모든 리스너의 수정 불가능한 Set을 반환하는 메서드
     * @return 등록된 모든 리스너의 Set
     */
    protected final Set<LISTENER_CLASS> getListeners() {
        return Collections.unmodifiableSet(this.mListeners);
    }
}
