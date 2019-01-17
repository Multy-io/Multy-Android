package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;

import java.util.ArrayList;

import io.multy.service.BluetoothService;

public class MagicSendViewModel extends BaseViewModel {
    private MutableLiveData<BluetoothService.BluetoothServiceMode> modeChangingLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> userCodesLiveData = new MutableLiveData<>();

    public MutableLiveData<BluetoothService.BluetoothServiceMode> getModeChangingLiveData() {
        return modeChangingLiveData;
    }

    public MutableLiveData<ArrayList<String>> getUserCodesLiveData() {
        return userCodesLiveData;
    }
}
