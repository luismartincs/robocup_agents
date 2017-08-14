package implementation.agents.policecentre;

/**
 * the policeOffice could have the information about the fire
 */
public class Fire {

    int xPosition;
    int yPosition;
    int victims;

    public int getxPosition() {
        return xPosition;
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public int getVictims() {
        return victims;
    }

    public void setVictims(int victims) {
        this.victims = victims;
    }

    public Fire(int xPosition, int yPosition, int victims) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.victims = victims;
    }

    public Fire(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }
}
