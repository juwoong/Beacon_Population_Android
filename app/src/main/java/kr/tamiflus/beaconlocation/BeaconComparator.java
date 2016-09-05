package kr.tamiflus.beaconlocation;

import org.altbeacon.beacon.Beacon;

import java.util.Comparator;


/**
 * Created by juwoong on 16. 1. 21..
 */
public class BeaconComparator implements Comparator<Beacon> {
    @Override
    public int compare(Beacon lhs, Beacon rhs) {
        return Double.compare(lhs.getDistance(), rhs.getDistance());
    }
}
