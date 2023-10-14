package cn.evole.plugins.civcraft.randomevents.components;


import cn.evole.plugins.civcraft.randomevents.RandomEventComponent;

public class MessageTown extends RandomEventComponent {

    @Override
    public void process() {
        String message = this.getString("message");
        sendMessage(message);
    }
}
