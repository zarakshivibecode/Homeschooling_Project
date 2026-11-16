package id.edu.homeschool.ui.design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Minimal line icons for the navigation. They are drawn with plain Java2D primitives so the
 * application ships without image assets and the icons stay crisp at every scale.
 */
public enum NavIcon {

    DASHBOARD, SUBJECT, SCHEDULE, MATERIAL, TASK, GOAL, EVALUATION, PROGRESS,
    TEACHERS, STUDENTS, PROFILE, REQUESTS, INVITATION, CONNECTION;

    private static final float STROKE = 1.6f;

    /** Draws the icon inside the given square. */
    public void paint(Graphics2D g, int x, int y, int size, Color color) {
        Graphics2D copy = (Graphics2D) g.create();
        Glass.prepare(copy);
        copy.setColor(color);
        copy.setStroke(new BasicStroke(STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float s = size;
        float u = s / 16f;

        switch (this) {
            case DASHBOARD -> {
                copy.draw(new RoundRectangle2D.Float(x + u, y + u, u * 6, u * 6, u, u));
                copy.draw(new RoundRectangle2D.Float(x + u * 9, y + u, u * 6, u * 6, u, u));
                copy.draw(new RoundRectangle2D.Float(x + u, y + u * 9, u * 6, u * 6, u, u));
                copy.draw(new RoundRectangle2D.Float(x + u * 9, y + u * 9, u * 6, u * 6, u, u));
            }
            case SUBJECT -> {
                copy.draw(poly(x + u * 2, y + u * 2, x + u * 7, y + u * 2, x + u * 7, y + u * 14, x + u * 2, y + u * 14));
                copy.draw(poly(x + u * 9, y + u * 3, x + u * 14, y + u * 3, x + u * 14, y + u * 14, x + u * 9, y + u * 14));
                copy.drawLine((int) (x + u * 4), (int) (y + u * 6), (int) (x + u * 6), (int) (y + u * 6));
                copy.drawLine((int) (x + u * 4), (int) (y + u * 9), (int) (x + u * 6), (int) (y + u * 9));
            }
            case SCHEDULE -> {
                copy.draw(new RoundRectangle2D.Float(x + u * 2, y + u * 3, u * 12, u * 11, u, u));
                copy.drawLine((int) (x + u * 2), (int) (y + u * 7), (int) (x + u * 14), (int) (y + u * 7));
                copy.drawLine((int) (x + u * 5), (int) (y + u * 1.5f), (int) (x + u * 5), (int) (y + u * 4.5f));
                copy.drawLine((int) (x + u * 11), (int) (y + u * 1.5f), (int) (x + u * 11), (int) (y + u * 4.5f));
            }
            case MATERIAL -> {
                copy.draw(poly(x + u * 3, y + u * 1.5f, x + u * 10, y + u * 1.5f, x + u * 13, y + u * 5,
                        x + u * 13, y + u * 14.5f, x + u * 3, y + u * 14.5f));
                copy.drawLine((int) (x + u * 6), (int) (y + u * 7), (int) (x + u * 10), (int) (y + u * 7));
                copy.drawLine((int) (x + u * 6), (int) (y + u * 10), (int) (x + u * 10), (int) (y + u * 10));
            }
            case TASK -> {
                copy.draw(new RoundRectangle2D.Float(x + u * 3, y + u * 3, u * 10, u * 11, u, u));
                copy.draw(new RoundRectangle2D.Float(x + u * 6, y + u * 1.5f, u * 4, u * 3, u * 0.8f, u * 0.8f));
                copy.drawLine((int) (x + u * 5.5f), (int) (y + u * 8), (int) (x + u * 10.5f), (int) (y + u * 8));
                copy.drawLine((int) (x + u * 5.5f), (int) (y + u * 11), (int) (x + u * 9), (int) (y + u * 11));
            }
            case GOAL -> {
                copy.draw(new Ellipse2D.Float(x + u * 2, y + u * 2, u * 12, u * 12));
                copy.draw(new Ellipse2D.Float(x + u * 5, y + u * 5, u * 6, u * 6));
                copy.fill(new Ellipse2D.Float(x + u * 7, y + u * 7, u * 2, u * 2));
            }
            case EVALUATION -> {
                copy.drawLine((int) (x + u * 2), (int) (y + u * 14), (int) (x + u * 14), (int) (y + u * 14));
                copy.draw(new RoundRectangle2D.Float(x + u * 3, y + u * 8, u * 3, u * 6, u * 0.6f, u * 0.6f));
                copy.draw(new RoundRectangle2D.Float(x + u * 7, y + u * 4, u * 3, u * 10, u * 0.6f, u * 0.6f));
                copy.draw(new RoundRectangle2D.Float(x + u * 11, y + u * 6, u * 3, u * 8, u * 0.6f, u * 0.6f));
            }
            case PROGRESS -> {
                copy.drawLine((int) (x + u * 2), (int) (y + u * 2), (int) (x + u * 2), (int) (y + u * 14));
                copy.drawLine((int) (x + u * 2), (int) (y + u * 14), (int) (x + u * 14), (int) (y + u * 14));
                copy.draw(poly(x + u * 4, y + u * 11, x + u * 7.5f, y + u * 7, x + u * 10, y + u * 9, x + u * 13.5f, y + u * 4));
                copy.fill(new Ellipse2D.Float(x + u * 12.7f, y + u * 3.2f, u * 1.6f, u * 1.6f));
            }
            case TEACHERS -> {
                copy.draw(new Ellipse2D.Float(x + u * 5, y + u * 2, u * 6, u * 6));
                copy.draw(poly(x + u * 2.5f, y + u * 14, x + u * 3.5f, y + u * 10, x + u * 12.5f, y + u * 10, x + u * 13.5f, y + u * 14));
            }
            case STUDENTS -> {
                copy.draw(poly(x + u * 8, y + u * 2, x + u * 15, y + u * 5, x + u * 8, y + u * 8, x + u * 1, y + u * 5));
                copy.draw(poly(x + u * 3.5f, y + u * 7, x + u * 3.5f, y + u * 12, x + u * 8, y + u * 14.5f, x + u * 12.5f, y + u * 12,
                        x + u * 12.5f, y + u * 7));
            }
            case PROFILE -> {
                copy.draw(new Ellipse2D.Float(x + u * 3, y + u * 2, u * 10, u * 10));
                copy.draw(new Ellipse2D.Float(x + u * 6, y + u * 4.5f, u * 4, u * 4));
                copy.draw(poly(x + u * 4.5f, y + u * 13, x + u * 5, y + u * 10, x + u * 11, y + u * 10, x + u * 11.5f, y + u * 13));
            }
            case REQUESTS -> {
                copy.draw(poly(x + u * 2, y + u * 9, x + u * 2, y + u * 3, x + u * 14, y + u * 3, x + u * 14, y + u * 9));
                copy.draw(poly(x + u * 2, y + u * 9, x + u * 5, y + u * 9, x + u * 6.5f, y + u * 11.5f, x + u * 9.5f, y + u * 11.5f,
                        x + u * 11, y + u * 9, x + u * 14, y + u * 9));
                copy.drawLine((int) (x + u * 8), (int) (y + u * 1.5f), (int) (x + u * 8), (int) (y + u * 6.5f));
                copy.draw(poly(x + u * 6, y + u * 4.5f, x + u * 8, y + u * 6.5f, x + u * 10, y + u * 4.5f));
            }
            case INVITATION -> {
                copy.draw(poly(x + u * 2, y + u * 5, x + u * 14, y + u * 5, x + u * 14, y + u * 11, x + u * 2, y + u * 11));
                copy.drawLine((int) (x + u * 9.5f), (int) (y + u * 5), (int) (x + u * 9.5f), (int) (y + u * 11));
                copy.drawLine((int) (x + u * 4), (int) (y + u * 8), (int) (x + u * 7), (int) (y + u * 8));
            }
            case CONNECTION -> {
                copy.draw(new Ellipse2D.Float(x + u * 1.5f, y + u * 5, u * 7, u * 6));
                copy.draw(new Ellipse2D.Float(x + u * 7.5f, y + u * 5, u * 7, u * 6));
            }
        }
        copy.dispose();
    }

    /** Builds a closed polygon from a flat list of coordinates. */
    private static Path2D.Float poly(float... coordinates) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(coordinates[0], coordinates[1]);
        for (int index = 2; index + 1 < coordinates.length; index += 2) {
            path.lineTo(coordinates[index], coordinates[index + 1]);
        }
        path.closePath();
        return path;
    }
}
