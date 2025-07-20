package org.libpag;

import java.util.Objects;

public class PAGRect {

    private float x1;
    private float y1;
    private float x2;
    private float y2;

    public PAGRect(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PAGRect pagRect = (PAGRect) o;
        return Float.compare(x1, pagRect.x1) == 0 && Float.compare(y1, pagRect.y1) == 0 && Float.compare(x2, pagRect.x2) == 0 && Float.compare(y2, pagRect.y2) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, y1, x2, y2);
    }
}
