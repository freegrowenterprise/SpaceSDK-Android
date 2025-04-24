/**
 * Position 클래스는 2차원 공간에서의 위치 정보를 나타내는 클래스입니다.
 * 이 클래스는 거리와 방위각을 사용하여 특정 지점의 위치를 표현합니다.
 */
package com.growspace.sdk.model;

public class Position {
    // 방위각 (0-360도)
    private float azimuth;
    
    // 거리 (미터 단위)
    private float distance;

    private float elevation;

    /**
     * Position 객체를 생성하는 생성자
     * @param f 거리 값 (미터 단위)
     * @param f2 방위각 값 (0-360도)
     */
    public Position(float f, float f2, float f3) {
        this.distance = f;
        this.azimuth = f2;
        this.elevation = f3;
    }

    /**
     * 현재 위치까지의 거리를 반환하는 메서드
     * @return 거리 값 (미터 단위)
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * 현재 위치의 방위각을 반환하는 메서드
     * @return 방위각 값 (0-360도)
     */
    public float getAzimuth() {
        return this.azimuth;
    }

    /**
     * 현재 위치의 고도를 반환하는 메서드
     * @return 고도 값 (미터 단위)
     */
    public float getElevation() {
        return this.elevation;
    }
}
