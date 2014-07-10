/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.main;

/**
 *
 * @author Gregor Altstädt
 */

import javax.swing.JFrame;
import javax.swing.JLabel;

import jpen.*;
import jpen.demo.StatusReport;
import jpen.event.PenListener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

public class JPenExample implements PenListener, MouseListener {

    public static void main(String... args) throws Throwable {
        new JPenExample();
    }

    JPenExample() {
        JLabel l = new JLabel("Move the pen or mouse over me!");
        PenManager pm = new PenManager(l);
        Collection<PenDevice> temp = pm.getDevices();
        System.out.println(temp + " - " +temp.size());
        //System.out.println(new StatusReport(pm));
        pm.pen.addListener(this);

        JFrame f = new JFrame("JPen Example");
        f.getContentPane().add(l);
        f.setSize(300, 300);
        f.setVisible(true);
    }

    //@Override
    public void penButtonEvent(PButtonEvent ev) {
        System.out.println(ev);
    }

    //@Override
    public void penKindEvent(PKindEvent ev) {
        //System.out.println(ev);
    }

    //@Override
    public void penLevelEvent(PLevelEvent ev) {
        //System.out.println(ev);
    }

    //@Override
    public void penScrollEvent(PScrollEvent ev) {
        //System.out.println(ev);
    }

    //@Override
    public void penTock(long availableMillis) {
        //System.out.println("TOCK - available period fraction: " + availableMillis);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        System.out.println("dsesessss");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("dseses");
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}