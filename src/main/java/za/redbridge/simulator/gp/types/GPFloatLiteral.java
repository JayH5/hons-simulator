package za.redbridge.simulator.gp.types;

import org.epochx.tools.util.TypeUtils;

import java.util.Optional;

/**
 * Created by xenos on 9/30/14.
 */
public class GPFloatLiteral extends GPFloat{
    public GPFloatLiteral(float v){
        super(v);
    }

    public String toString(){
        return "FL" + value;
    }
}
