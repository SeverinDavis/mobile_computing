// ITrackerService.aidl
package org.example.boraozgen.gpsapp;

// Interface for the communication between the activity and the service

interface ITrackerService {
    double getLatitude();
    double getLongitude();
    double getSpeed();
    double getDistance();
}
