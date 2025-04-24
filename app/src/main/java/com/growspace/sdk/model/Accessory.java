/**
 * Accessory 클래스는 UWB 액세서리 장치의 정보를 나타내는 클래스입니다.
 * 이 클래스는 장치의 이름, MAC 주소, 별칭 등의 정보를 관리합니다.
 */
package com.growspace.sdk.model;

public class Accessory {
    // 장치의 사용자 정의 별칭
    private String alias;
    
    // 장치의 MAC 주소
    private String mac;
    
    // 장치의 기본 이름
    private String name;

    /**
     * 기본 생성자
     * 모든 필드를 null로 초기화합니다.
     */
    public Accessory() {
    }

    /**
     * 모든 필드를 초기화하는 생성자
     * @param str 장치의 기본 이름
     * @param str2 장치의 MAC 주소
     * @param str3 장치의 사용자 정의 별칭
     */
    public Accessory(String str, String str2, String str3) {
        this.name = str;
        this.mac = str2;
        this.alias = str3;
    }

    /**
     * 장치의 기본 이름을 반환하는 메서드
     * @return 장치의 기본 이름
     */
    public String getName() {
        return this.name;
    }

    /**
     * 장치의 기본 이름을 설정하는 메서드
     * @param str 설정할 장치의 기본 이름
     */
    public void setName(String str) {
        this.name = str;
    }

    /**
     * 장치의 MAC 주소를 반환하는 메서드
     * @return 장치의 MAC 주소
     */
    public String getMac() {
        return this.mac;
    }

    /**
     * 장치의 MAC 주소를 설정하는 메서드
     * @param str 설정할 장치의 MAC 주소
     */
    public void setMac(String str) {
        this.mac = str;
    }

    /**
     * 장치의 사용자 정의 별칭을 반환하는 메서드
     * @return 장치의 사용자 정의 별칭
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * 장치의 사용자 정의 별칭을 설정하는 메서드
     * @param str 설정할 장치의 사용자 정의 별칭
     */
    public void setAlias(String str) {
        this.alias = str;
    }
}
