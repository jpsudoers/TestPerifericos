package cn.eas.usdk.demo.view.dock;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.RadioGroup;

import com.usdk.apiservice.aidl.data.IntValue;
import com.usdk.apiservice.aidl.dock.DockError;
import com.usdk.apiservice.aidl.dock.DockName;
import com.usdk.apiservice.aidl.dock.GetDmzStateListener;
import com.usdk.apiservice.aidl.dock.UWifiDock;

import cn.eas.usdk.demo.DeviceHelper;
import cn.eas.usdk.demo.R;

public class WifDockActivity extends BaseDockActivity {

    private UWifiDock dock;

    private boolean dmzEnabled;

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_wifi_dock);
        setTitle("Wifi Dock Module");

        initDeviceInstance();

        initRadio();
    }

    protected void initDeviceInstance() {
        dock = DeviceHelper.me().getWifiDock();
    }

    private void initRadio() {
        RadioGroup rgDmz = bindViewById(R.id.rg_dmz);
        rgDmz.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int btnId = radioGroup.getCheckedRadioButtonId();
                dmzEnabled = btnId == R.id.rb_dmz_enable;
            }
        });
    }

    public void getDockStatus(View v) {
        outputBlueText(">>> getDockStatus");
        try {
            IntValue status = new IntValue();
            int ret = dock.getDockStatus(status);
            if (ret != DockError.SUCCESS) {
                outputRedText(getErrorDetail(ret));
                return;
            }
            outputText("=> status : " + getStatusDescription(status.getData()));
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void defaultPairUI(View v) {
        outputBlueText(">>> callPairActivity");
        try {
            int ret = dock.callPairActivity();
            if (ret != DockError.SUCCESS) {
                outputRedText(getErrorDetail(ret));
                return;
            }
            outputText("=> callPairActivity success");
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void customPairUI(View v) {
        startActivity(WifiDockCustomPairActivity.class);
    }

    public void startDockEthernetDemo(View v) {
        startActivity(DockEthernetActivity.class);
    }

    public void startDockPortDemo(View v) {
        startActivity(DockPortActivity.class, DockName.WIFI_DOCK);
    }

    public void setDmzEnabled(View v) {
        outputBlueText(">>> setDmzEnabled: " + dmzEnabled);
        try {
            int ret = dock.setDmzEnabled(dmzEnabled);
            outputText("=> setDmzEnabled, ret = " + ret);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getDmzEnableState(View v) {
        outputBlueText(">>> getDmzEnableState");
        try {
            dock.getDmzEnableState(new GetDmzStateListener.Stub() {
                @Override
                public void onSuccess(int state) throws RemoteException {
                    outputText("=> getDmzEnableState success, state = " + state);
                }

                @Override
                public void onError(int errorCode) throws RemoteException {
                    outputRedText("=> getDmzEnableState error, errorCode = " + errorCode);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
