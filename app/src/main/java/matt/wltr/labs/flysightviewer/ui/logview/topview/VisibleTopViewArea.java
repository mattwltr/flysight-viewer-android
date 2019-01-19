package matt.wltr.labs.flysightviewer.ui.logview.topview;

import com.scichart.data.model.DoubleRange;

class VisibleTopViewArea {

    private static final double OFFSET = 1000F;

    private DoubleRange rangeX;

    private DoubleRange rangeY;

    /**
     * @param minX actual visible minimum of x axis
     * @param maxX actual visible maximum of x axis
     * @param minY actual visible minimum of y axis
     * @param maxY actual visible maximum of y axis
     */
    VisibleTopViewArea(double minX, double maxX, double minY, double maxY) {

        double yDifference = maxY - minY;

        double xDifference = maxX - minX;

        if (xDifference > yDifference) {
            double relativeDifference = (xDifference - yDifference) / 2D;
            minY -= relativeDifference;
            maxY += relativeDifference;
        } else if (xDifference < yDifference) {
            double relativeDifference = (yDifference - xDifference) / 2D;
            minX -= relativeDifference;
            maxX += relativeDifference;
        }

        minY -= OFFSET;
        maxY += OFFSET;
        minX -= OFFSET;
        maxX += OFFSET;

        rangeX = new DoubleRange(minX, maxX);
        rangeY = new DoubleRange(minY, maxY);
    }

    DoubleRange getRangeX() {
        return rangeX;
    }

    public double getCenterX() {
        return rangeX.getMinAsDouble() + (rangeX.getDiff() / 2);
    }

    DoubleRange getRangeY() {
        return rangeY;
    }

    public double getCenterY() {
        return rangeY.getMinAsDouble() + (rangeY.getDiff() / 2);
    }
}
