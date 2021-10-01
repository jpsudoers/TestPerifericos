package cn.eas.usdk.demo.view.pinpad;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;

import com.usdk.apiservice.aidl.pinpad.KAPId;
import com.usdk.apiservice.aidl.pinpad.KeyAlgorithm;
import com.usdk.apiservice.aidl.pinpad.KeyHandle;
import com.usdk.apiservice.aidl.pinpad.KeySystem;
import com.usdk.apiservice.aidl.pinpad.KeyType;
import com.usdk.apiservice.aidl.pinpad.MacMode;
import com.usdk.apiservice.aidl.pinpad.OnPinEntryListener;
import com.usdk.apiservice.aidl.pinpad.PinBlockFormat;
import com.usdk.apiservice.aidl.pinpad.PinpadData;

import cn.eas.usdk.demo.R;
import cn.eas.usdk.demo.constant.DemoConfig;
import cn.eas.usdk.demo.util.BytesUtil;

/**
 * dukpt system
 * 1. Only 16 bytes of key.
 * 2. A set of dukpt keys includes pinkey, Mackey and tdkey. If you want to use multiple groups, download multiple sets of IK.
 * 3. The DES key does not apply to DUKPT, so the DES encryption-decryption-related interface is not supported.
 * 4. Usually download IK through TKI.
 * 5. There is no key type in this system.
 */
public class DUKPTActivity extends BasePinpadActivity {
    private int keyId = KEYID_MAIN;
    private static final String TEST_DUKPT_AES256_IK = "CE9CE0C101D1138F97FB6CAD4DF045A7083D4EAE2D35A31789D01CCF0949550F";
    private static final String TEST_DUKPT_AES_KSN = "123456789012345600000001";
    private KeyHandle dukptKeyHandle;

    @Override
    public int getKeySystem() {
        return KeySystem.KS_DUKPT;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.activity_pinpad_dukpt);
        setTitle("Pinpad DUKPT Module");

        createHandle();
        open();
    }

    public void isKeyExist(View v) {
        isKeyExist(new int[] {keyId});
    }

    public void deleteKey(View v) {
        deleteKey(new int[] {keyId});
    }

    /**
     * This step is usually done by LKI
     */
    public void loadInitKey(View v) {
        try {
            outputBlueText(">>> format");
            boolean isSucc = pinpadLimited.format();
            if (isSucc) {
                outputText("format success");
            } else {
                outputPinpadError("format fail");
                return;
            }

            outputBlueText(">>> loadPlainTextKey");
//            String key = "11111111111111111111111111111111";
            String key = "6AC292FAA1315B4D858AB3A3D7D5933A";// From X9.24-2009 A.4
            isSucc = pinpadLimited.loadPlainTextKey(KeyType.MAIN_KEY, keyId, BytesUtil.hexString2Bytes(key));
            if (isSucc) {
                outputText(String.format("loadPlainTextKey(MAIN_KEY, keyId = %s) success", keyId));
            } else {
                outputPinpadError("loadPlainTextKey fail");
                return;
            }

            outputBlueText(">>> switchToWorkMode");
            isSucc = pinpadLimited.switchToWorkMode();
            if (isSucc) {
                outputText("switchToWorkMode success");
            } else {
                outputPinpadError("switchToWorkMode fail");
                return;
            }

            initDukptIkKsn();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void initDukptIkKsn() throws RemoteException {
        outputBlueText(">>> initDukptIkKsn");
        //byte[] ksnData = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        byte[] ksnData = new byte[] {(byte) 0xff, (byte)0xff, (byte)0x98, 0x76, 0x54, 0x32, 0x10, (byte)0xe0, 0x00, 0x00};// From X9.24-2009 A.4
        boolean isSucc = pinpad.initDukptIkKsn(keyId, ksnData);
        if (isSucc) {
            outputText("initDukptIkKsn success");
        } else {
            outputPinpadError("initDukptIkKsn fail");
        }
    }

    /**
     * Derive a group of keys: including pinkey, Mackey, tdkey
     */
    public void increaseDukptKsn(View v) {
        outputBlueText(">>> increaseDukptKsn");
        try {
            boolean isSucc = pinpad.increaseDukptKsn(keyId);
            if (isSucc) {
                outputText("increaseDukptKsn success");
            } else {
                outputPinpadError("increaseDukptKsn fail");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     *  Note: DUKPT only supports Mac_ ALG_ ISO9797
     */
    public void calcMAC(View v) {
        calcMAC(keyId, MacMode.MAC_PADDING_MODE_1, MacMode.MAC_ALG_ISO9797);
    }

    public void calcMACISO16609(View v) {
        calcMAC(keyId, MacMode.MAC_PADDING_MODE_1, MacMode.MAC_ALG_ISO16609_1);
    }

    public void encryptMagTrack(View v) {
        encryptMagTrack(keyId);
    }

    public void startPinEntry(View v) {
        startPinEntry(keyId);
    }

    public void dukpt2017AESTest(View v) {
        try {
            outputBlueText(">>> format");
            boolean isSucc = pinpadLimited.format();
            if (isSucc) {
                outputText("format success");
            } else {
                outputPinpadError("format fail");
                return;
            }

            loadIK(KeyAlgorithm.KA_AES);
            pinEntry(KeyAlgorithm.KA_AES, 32);

        } catch (Exception e) {
            handleException(e);
        }

    }

    private void createHandle() {
        dukptKeyHandle = new KeyHandle(new KAPId(DemoConfig.REGION_ID, DemoConfig.KAP_NUM), KeySystem.KS_DUKPT, keyId);
    }

    private void loadIK(char algorithm) throws RemoteException{
        pinpad.setKeyAlgorithm(algorithm);

        outputBlueText(">>> loadPlainTextKey");
        boolean isSucc = pinpadLimited.loadPlainTextKey(KeyType.MAIN_KEY, keyId, BytesUtil.hexString2Bytes(TEST_DUKPT_AES256_IK));
        if (isSucc) {
            outputText(String.format("loadPlainTextKey(MAIN_KEY, keyId = %s) success", keyId));
        } else {
            outputPinpadError("loadPlainTextKey fail");
            return;
        }

        byte[] kcv = pinpad.getCmacKcv(dukptKeyHandle);
        outputText("kcv = " + BytesUtil.bytes2HexString(kcv));

        outputBlueText(">>> initDukptIkKsnByKeyHandle");
        isSucc = pinpad.initDukptIkKsnByKeyHandle(dukptKeyHandle, BytesUtil.hexString2Bytes(TEST_DUKPT_AES_KSN), false);
        if (isSucc) {
            outputText("initDukptIkKsnByKeyHandle success");
        } else {
            outputPinpadError("initDukptIkKsnByKeyHandle fail");
        }

        outputBlueText(">>> getDukptCurKsnByKeyHandle");
        byte[] ksn = pinpad.getDukptCurKsnByKeyHandle(dukptKeyHandle, false);
        outputText("ksn = " + BytesUtil.bytes2HexString(ksn));
    }


    private void pinEntry(char dukptWorkKeyAlgorithm, int dukptWorkKeySize) throws RemoteException {

        outputBlueText(">>> startPinEntryAES256");
        try {
            pinpad.setDukptWorkKey(dukptWorkKeyAlgorithm, dukptWorkKeySize);
            byte pinBlockFormat = dukptWorkKeyAlgorithm == KeyAlgorithm.KA_AES ? PinBlockFormat.BLOCK_FORMAT_4 : PinBlockFormat.BLOCK_FORMAT_0;
            Bundle param = new Bundle();
            param.putInt(PinpadData.TIMEOUT, 350);
            param.putInt(PinpadData.BETWEEN_PINKEY_TIMEOUT, 70);
            param.putByteArray(PinpadData.PIN_LIMIT, new byte[] { 0, 4});
            param.putByteArray(PinpadData.PAN_BLOCK, new byte[] { 0, 4, 5});
            param.putByte(PinpadData.PIN_BLOCK_FORMAT, pinBlockFormat);

            pinpad.startPinEntryByKeyHandle(dukptKeyHandle, param, new OnPinEntryListener.Stub() {
                @Override
                public void onInput(int pinLen, int v) throws RemoteException {
                    outputBlackText("onInput | pinLen = " + pinLen + ", v = " + v);
                }

                @Override
                public void onConfirm(byte[] pinBlock, boolean isNonePin) throws RemoteException {
                    outputText("/// onConfirm | KSN = " + BytesUtil.bytes2HexString(getCurrentKsn()));
                    outputText("/// onConfirm | PIN block (hex) = " + BytesUtil.bytes2HexString(pinBlock));
                }

                @Override
                public void onCancel() throws RemoteException {
                    outputText("=> onCancel");
                }

                @Override
                public void onError(int err) throws RemoteException {
                    outputRedText("=> onError: " + getErrorDetail(err));
                }
            });
        } catch (Exception e) {
            handleException(e);
        }
    }

    private byte[] getCurrentKsn() {
        try {
            return pinpad.getDukptCurKsn(keyId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }
}
