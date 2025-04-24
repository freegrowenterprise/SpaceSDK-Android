package com.growspace.sdk.permissions;

import android.app.Activity;
import androidx.core.app.ActivityCompat;

import com.growspace.sdk.BaseObservable;

/**
 * PermissionHelper 클래스는 Android 런타임 권한을 요청하고 결과를
 * 처리하여 등록된 리스너에게 알려주는 역할을 합니다.
 * - ActivityCompat을 사용해 권한 체크 및 요청을 수행
 * - onRequestPermissionsResult를 통해 콜백을 분기하여 Listener에 전달
 * - BaseObservable을 상속받아 여러 Listener 관리 지원
 */
public class PermissionHelper extends BaseObservable<PermissionHelper.Listener> {
    // 권한 체크 및 요청에 사용할 Activity 참조
    private final Activity mActivity;

    /**
     * 권한 결과를 수신할 Listener 인터페이스
     */
    public interface Listener {
        /**
         * 권한이 거부된 경우 호출
         * @param permission 거부된 권한 문자열 (예: android.permission.CAMERA)
         * @param requestCode 요청 시 사용된 코드
         */
        void onPermissionDeclined(String permission, int requestCode);

        /**
         * 권한이 거부되고 "다시 묻지 않음"이 체크된 경우 호출
         * @param permission 거부된 권한 문자열
         * @param requestCode 요청 시 사용된 코드
         */
        void onPermissionDeclinedDontAskAgain(String permission, int requestCode);

        /**
         * 권한이 허용된 경우 호출
         * @param permission 허용된 권한 문자열
         * @param requestCode 요청 시 사용된 코드
         */
        void onPermissionGranted(String permission, int requestCode);
    }

    /**
     * 생성자
     * @param activity 권한 요청 및 결과 처리를 위한 Activity
     */
    public PermissionHelper(Activity activity) {
        // 전달받은 Activity를 내부 필드에 저장
        this.mActivity = activity;
    }

    /**
     * 특정 권한이 이미 허용되었는지 확인
     * @param permission 확인할 권한 문자열
     * @return 허용된 경우 true, 아니면 false
     */
    public boolean hasPermission(String permission) {
        // ActivityCompat.checkSelfPermission이 PERMISSION_GRANTED(0)인지 비교
        return ActivityCompat.checkSelfPermission(this.mActivity, permission) == 0;
    }

    /**
     * 단일 권한을 요청
     * @param permission 요청할 권한 문자열
     * @param requestCode 권한 요청 결과 구분을 위한 코드
     */
    public void requestPermission(String permission, int requestCode) {
        // ActivityCompat.requestPermissions로 권한 요청
        ActivityCompat.requestPermissions(this.mActivity, new String[]{permission}, requestCode);
    }

    /**
     * 다중 권한을 한 번에 요청
     * @param permissions 요청할 권한 문자열 배열
     * @param requestCode 권한 요청 결과 구분을 위한 코드
     */
    public void requestPermissions(String[] permissions, int requestCode) {
        // ActivityCompat.requestPermissions로 다중 권한 요청
        ActivityCompat.requestPermissions(this.mActivity, permissions, requestCode);
    }

    /**
     * Activity의 onRequestPermissionsResult에서 호출하여 권한 결과를 처리
     * @param requestCode 요청 시 전달된 코드
     * @param permissions 요청한 권한 문자열 배열
     * @param grantResults 결과 배열 (PackageManager.PERMISSION_GRANTED 또는 PERMISSION_DENIED)
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 요청된 권한이 하나도 없으면 아무 처리도 하지 않음
        if (permissions.length < 1) {
            return;
        }
        // 각 권한별로 결과를 순회
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int result = grantResults[i];
            if (result == 0) {
                // PERMISSION_GRANTED(0)이면 허용 콜백 호출
                notifyPermissionGranted(permission, requestCode);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, permission)) {
                // 거부되었으나 다시 묻기 가능 시 기본 거부 콜백
                notifyPermissionDeclined(permission, requestCode);
            } else {
                // 거부되고 다시 묻지 않음 체크 시 해당 콜백
                notifyPermissionDeclinedDontAskAgain(permission, requestCode);
            }
        }
    }

    /**
     * "다시 묻지 않음"과 함께 권한이 거부된 경우 모든 리스너에 알림
     * @param permission 거부된 권한 문자열
     * @param requestCode 요청 코드
     */
    private void notifyPermissionDeclinedDontAskAgain(String permission, int requestCode) {
        // 등록된 각 리스너에 onPermissionDeclinedDontAskAgain 호출
        for (Listener listener : getListeners()) {
            listener.onPermissionDeclinedDontAskAgain(permission, requestCode);
        }
    }

    /**
     * 권한이 거부된 경우 모든 리스너에 알림
     * @param permission 거부된 권한 문자열
     * @param requestCode 요청 코드
     */
    private void notifyPermissionDeclined(String permission, int requestCode) {
        // 등록된 각 리스너에 onPermissionDeclined 호출
        for (Listener listener : getListeners()) {
            listener.onPermissionDeclined(permission, requestCode);
        }
    }

    /**
     * 권한이 허용된 경우 모든 리스너에 알림
     * @param permission 허용된 권한 문자열
     * @param requestCode 요청 코드
     */
    private void notifyPermissionGranted(String permission, int requestCode) {
        // 등록된 각 리스너에 onPermissionGranted 호출
        for (Listener listener : getListeners()) {
            listener.onPermissionGranted(permission, requestCode);
        }
    }
}
