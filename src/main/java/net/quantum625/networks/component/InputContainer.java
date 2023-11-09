package net.quantum625.networks.component;

import net.quantum625.networks.utils.Location;

public class InputContainer extends BaseComponent {

    public InputContainer(Location pos) {
        super(pos);
    }

    private int range = 0;

    @Override
    public ComponentType getType() {return ComponentType.INPUT;}
}
