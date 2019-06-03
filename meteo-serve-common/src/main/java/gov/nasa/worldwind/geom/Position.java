package gov.nasa.worldwind.geom;

import java.util.*;

/**
 * @author tag
 * @version $Id: Position.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class Position extends LatLon
{
    public static final Position ZERO = new Position(Angle.ZERO, Angle.ZERO, 0d);

    public final double elevation;

    public static Position fromRadians(double latitude, double longitude, double elevation)
    {
        return new Position(Angle.fromRadians(latitude), Angle.fromRadians(longitude), elevation);
    }

    public static Position fromDegrees(double latitude, double longitude, double elevation)
    {
        return new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), elevation);
    }

    public static Position fromDegrees(double latitude, double longitude)
    {
        return new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), 0);
    }

    public Position(Angle latitude, Angle longitude, double elevation)
    {
        super(latitude, longitude);
        this.elevation = elevation;
    }

    public Position(LatLon latLon, double elevation)
    {
        super(latLon);
        this.elevation = elevation;
    }

    // A class that makes it easier to pass around position lists.
    public static class PositionList
    {
        public List<? extends Position> list;

        public PositionList(List<? extends Position> list)
        {
            this.list = list;
        }
    }

    /**
     * Obtains the elevation of this position
     *
     * @return this position's elevation
     */
    public double getElevation()
    {
        return this.elevation;
    }

    /**
     * Obtains the elevation of this position
     *
     * @return this position's elevation
     */
    public double getAltitude()
    {
        return this.elevation;
    }

    public Position add(Position that)
    {
        Angle lat = Angle.normalizedLatitude(this.latitude.add(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.add(that.longitude));

        return new Position(lat, lon, this.elevation + that.elevation);
    }

    public Position subtract(Position that)
    {
        Angle lat = Angle.normalizedLatitude(this.latitude.subtract(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.subtract(that.longitude));

        return new Position(lat, lon, this.elevation - that.elevation);
    }

    public static boolean positionsCrossDateLine(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            throw new IllegalArgumentException("nullValue.PositionsListIsNull");
        }

        Position pos = null;
        for (Position posNext : positions)
        {
            if (pos != null)
            {
                // A segment cross the line if end pos have different longitude signs
                // and are more than 180 degress longitude apart
                if (Math.signum(pos.getLongitude().degrees) != Math.signum(posNext.getLongitude().degrees))
                {
                    double delta = Math.abs(pos.getLongitude().degrees - posNext.getLongitude().degrees);
                    if (delta > 180 && delta < 360)
                        return true;
                }
            }
            pos = posNext;
        }

        return false;
    }

    /**
     * Computes a new set of positions translated from a specified reference position to a new reference position.
     *
     * @param oldPosition the original reference position.
     * @param newPosition the new reference position.
     * @param positions   the positions to translate.
     *
     * @return the translated positions, or null if the positions could not be translated.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public static List<Position> computeShiftedPositions(Position oldPosition, Position newPosition,
        Iterable<? extends Position> positions)
    {
        // TODO: Account for dateline spanning
        if (oldPosition == null || newPosition == null)
        {
            throw new IllegalArgumentException("nullValue.PositionIsNull");
        }

        if (positions == null)
        {
            throw new IllegalArgumentException("nullValue.PositionsListIsNull");
        }

        ArrayList<Position> newPositions = new ArrayList<Position>();

        double elevDelta = newPosition.getElevation() - oldPosition.getElevation();

        for (Position pos : positions)
        {
            Angle distance = LatLon.greatCircleDistance(oldPosition, pos);
            Angle azimuth = LatLon.greatCircleAzimuth(oldPosition, pos);
            LatLon newLocation = LatLon.greatCircleEndPosition(newPosition, azimuth, distance);
            double newElev = pos.getElevation() + elevDelta;

            newPositions.add(new Position(newLocation, newElev));
        }

        return newPositions;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        Position position = (Position) o;

        //noinspection RedundantIfStatement
        if (Double.compare(position.elevation, elevation) != 0)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        long temp;
        temp = elevation != +0.0d ? Double.doubleToLongBits(elevation) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String toString()
    {
        return "(" + this.latitude.toString() + ", " + this.longitude.toString() + ", " + this.elevation + ")";
    }
}
