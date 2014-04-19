/*
 * Copyright 2014 Parth Sane

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package code.laerstudios.openairplay;

import java.util.Comparator;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

import code.laerstudios.openairplay.BrowseActivity.BrowseRegistryListener;
import code.laerstudios.openairplay.BrowseActivity.DeviceDisplay;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class FirstPane extends ListFragment {
	private ArrayAdapter<DeviceDisplay> listAdapter;

    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    ViewGroup myViewGroup;
    private AndroidUpnpService upnpService;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            // Refresh the list with all known devices
            listAdapter.clear();
            for (Device<?, ?, ?> device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Search asynchronously for all devices
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = new View(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root= inflater.inflate(R.layout.firstlayout,container,false);
        root.setBackgroundColor(Color.GRAY);
        setHasOptionsMenu(true);
        myViewGroup=container;
        
        listAdapter =
                new ArrayAdapter<code.laerstudios.openairplay.FirstPane.DeviceDisplay>(
                    inflater.getContext()
                    , android.R.layout.list_content
                );
        
            setListAdapter(listAdapter);
        

        
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

       //inflater.inflate(R.menu.base_menu, menu);
    }
    public void OnActivityCreated(Bundle savedInstanceState){
    	
    }
    @Override
    public void onStart() {
        super.onStart();
        
        Toast.makeText(getActivity(), "BrowserUpnpService Started",Toast.LENGTH_SHORT).show();
        getActivity().bindService(
                new Intent(getActivity(), BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
        
    }
    @Override
    public void onStop() {
        super.onStop();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        getActivity().unbindService(serviceConnection);
        Toast.makeText(getActivity(), "BrowserUpnpService Stopped",Toast.LENGTH_SHORT).show();
    }
    protected class BrowseRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */
    	
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
          /*  Toast.makeText(context, "Discovery failed of '" + device.getDisplayString() + "': " +
                    (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors",Toast.LENGTH_SHORT);
                    ),
                    true
            );*/
            deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device<?, ?, ?> device) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    DeviceDisplay d = new DeviceDisplay(device);

                    int position = listAdapter.getPosition(d);
                    if (position >= 0) {
                        // Device already in the list, re-set new value at same position
                        listAdapter.remove(d);
                        listAdapter.insert(d, position);
                    } else {
                        listAdapter.add(d);
                    }

                    // Sort it?
                    // listAdapter.sort(DISPLAY_COMPARATOR);
                    // listAdapter.notifyDataSetChanged();
                }
            });
        }

        public void deviceRemoved(final Device<?, ?, ?> device) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    listAdapter.remove(new DeviceDisplay(device));
                }
            });
        }
    }
    protected void searchNetwork() {
        if (upnpService == null) return;
        Toast.makeText(getActivity(), R.string.searching_lan, Toast.LENGTH_SHORT).show();
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
    }
    protected class DeviceDisplay {

        Device<?, ?, ?> device;

        public DeviceDisplay(Device<?, ?, ?> device) {
            this.device = device;
        }

        public Device<?, ?, ?> getDevice() {
            return device;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceDisplay that = (DeviceDisplay) o;
            return device.equals(that.device);
        }

        @Override
        public int hashCode() {
            return device.hashCode();
        }

        @Override
        public String toString() {
            String name =
                    device.getDetails() != null && device.getDetails().getFriendlyName() != null
                            ? device.getDetails().getFriendlyName()
                            : device.getDisplayString();
            // Display a little star while the device is being loaded (see performance optimization earlier)
            return device.isFullyHydrated() ? name : name + " *";
        }
    }

    static final Comparator<DeviceDisplay> DISPLAY_COMPARATOR =
            new Comparator<DeviceDisplay>() {
                public int compare(DeviceDisplay a, DeviceDisplay b) {
                    return a.toString().compareTo(b.toString());
                }
            };
}
