package code.laerstudios.openairplay;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AirplayActivity extends FragmentActivity implements PropertyChangeListener {

	 private static final Logger log = Logger.getLogger(AirplayActivity.class.getName());

	    private AndroidUpnpService upnpService;

	    private UDN udn = UDN.uniqueSystemIdentifier("appletv");

	    private ServiceConnection serviceConnection = new ServiceConnection() {

	        public void onServiceConnected(ComponentName className, IBinder service) {
	            upnpService = (AndroidUpnpService) service;

	            LocalService<Airplay> airPlayService = getAirplayService();

	            // Register the device when this activity binds to the service for the first time
	            if (airPlayService == null) {
	                try {
	                    LocalDevice appletv = createDevice();

	                    Toast.makeText(AirplayActivity.this, R.string.registering_demo_device, Toast.LENGTH_SHORT).show();
	                    upnpService.getRegistry().addDevice(appletv);

	                    airPlayService = getAirplayService();

	                } catch (Exception ex) {
	                    log.log(Level.SEVERE, "Creating demo device failed", ex);
	                    Toast.makeText(AirplayActivity.this, R.string.create_demo_failed, Toast.LENGTH_SHORT).show();
	                    return;
	                }
	            }

	            // Obtain the state of the power switch and update the UI
	            setLightbulb(airPlayService.getManager().getImplementation().getStatus());

	            // Start monitoring the power switch
	            airPlayService.getManager().getImplementation().getPropertyChangeSupport()
	                    .addPropertyChangeListener(AirplayActivity.this);

	        }

	        public void onServiceDisconnected(ComponentName className) {
	            upnpService = null;
	        }
	    };
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getApplicationContext().bindService(
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop monitoring the power switch
        LocalService<Airplay> airPlayService = getAirplayService();
        if (airPlayService != null)
            airPlayService.getManager().getImplementation().getPropertyChangeSupport()
                    .removePropertyChangeListener(this);

        getApplicationContext().unbindService(serviceConnection);
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("status")) {
            log.info("Turning light: " + event.getNewValue());
            setLightbulb((Boolean) event.getNewValue());
        }
    }

    protected void setLightbulb(final boolean on) {
        runOnUiThread(new Runnable() {
            public void run() {
                
            }
        });
    }

    @SuppressWarnings("unchecked")
	protected LocalService<Airplay> getAirplayService() {
        if (upnpService == null)
            return null;

        LocalDevice airplayDevice;
        if ((airplayDevice = upnpService.getRegistry().getLocalDevice(udn, true)) == null)
            return null;

        return (LocalService<Airplay>)
                airplayDevice.findService(new UDAServiceType("airplay", 1));
    }

    @SuppressWarnings("unchecked")
	protected LocalDevice createDevice()
            throws ValidationException, LocalServiceBindingException {

        DeviceType type =
                new UDADeviceType("appletv", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "Apple TV",
                        new ManufacturerDetails("Apple"),
                        new ModelDetails("Apple Tv", "Your very own Apple TV", "v1")
                );

        @SuppressWarnings("rawtypes")
		LocalService service =
                new AnnotationLocalServiceBinder().read(Airplay.class);

        service.setManager(
                new DefaultServiceManager<Airplay>(service, Airplay.class)
        );

        return new LocalDevice(
                new DeviceIdentity(udn),
                type,
                details,
               
                service
        );
    }

   

}
