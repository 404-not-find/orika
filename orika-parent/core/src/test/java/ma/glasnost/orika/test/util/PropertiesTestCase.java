package ma.glasnost.orika.test.util;

import java.util.Map;

import javax.swing.JButton;

import ma.glasnost.orika.impl.util.PropertyUtil;
import ma.glasnost.orika.metadata.Property;

import org.junit.Ignore;
import org.junit.Test;

public class PropertiesTestCase {
    
    @Test
    @Ignore
    public void testJButton() {
        
        Map<String, Property> properties = PropertyUtil.getProperties(JButton.class);
        
        // TODO To rewrite
    }
    
    public class Point {
        private int x, y;
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
    }
    
}
