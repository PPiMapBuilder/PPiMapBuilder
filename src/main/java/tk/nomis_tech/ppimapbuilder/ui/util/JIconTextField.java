package tk.nomis_tech.ppimapbuilder.ui.util;

/*
 * Copyright 2010 Georgios Migdos <cyberpython@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

import java.awt.Graphics;
import java.awt.Insets;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
 
/**
 * 
 *@authorGeorgios Migdos <cyberpython@gmail.com> 
 */
public class JIconTextField extends JTextField{
 
    private Icon icon;
    private Insets dummyInsets;
 
    public JIconTextField(){
        super();
    	System.out.println("#JSearch 1");
        this.icon = null;
 
        Border border = UIManager.getBorder("TextField.border");
    	System.out.println("#JSearch 1.1");
        JTextField dummy = new JTextField();
    	System.out.println("#JSearch 1.2");
        this.dummyInsets = new Insets(0, 0, 0, 0);//border.getBorderInsets(dummy);
    	System.out.println("#JSearch 1.9");
    }
 
    public void setIcon(Icon icon){
        this.icon = icon;
    }
    
	public void setIcon(URL resource) {
		System.out.print("#url : ");System.out.println(resource);
		this.icon = new ImageIcon(resource);
	}
 
    public Icon getIcon(){
        return this.icon;
    }
 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
 
        int textX = 2;
 
        if(this.icon!=null){
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            int x = dummyInsets.left + 5;//this is our icon's x
            textX = x+iconWidth+2; //this is the x where text should start
            int y = (this.getHeight() - iconHeight)/2;
            icon.paintIcon(this, g, x, y);
        }
 
        setMargin(new Insets(2, textX, 2, 2));
 
    }
 
}