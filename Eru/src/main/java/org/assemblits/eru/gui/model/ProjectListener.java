package org.assemblits.eru.gui.model;

import javafx.collections.ListChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assemblits.eru.comm.bus.Fieldbus;
import org.assemblits.eru.comm.bus.Modbus;
import org.assemblits.eru.comm.modbus.ModbusDeviceReading;
import org.assemblits.eru.entities.Connection;
import org.assemblits.eru.entities.Device;
import org.assemblits.eru.entities.Display;
import org.assemblits.eru.entities.Tag;
import org.assemblits.eru.gui.dynamo.DynamoExtractor;
import org.assemblits.eru.gui.dynamo.base.Dynamo;
import org.assemblits.eru.tag.TagBus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by mtrujillo on 9/9/2015.
 */
@Slf4j
@Component
@Data
public class ProjectListener {

    private final Fieldbus fieldbus;
    private final TagBus tagsHandler;
    private ProjectModel projectModel;
    private boolean listening;

    public void listen(){
        if (listening) return;
        listenDevicesChanges();
        listenConnectionsChanges();
        listenTagsChanges();
        listenUsersChanges();
        listenDisplaysChanges();
        listening = true;
    }

    private void listenDevicesChanges(){
        // TODO
    }

    private void listenConnectionsChanges(){
        // Current Connections
        this.projectModel.getConnections().forEach(this::installLink);
        // Future Connections
        this.projectModel.getConnections().addListener((ListChangeListener<Connection>) c -> {
            while (c.next()) {
                for (Connection newConnection : c.getAddedSubList()) {
                    installLink(newConnection);
                }
            }
        });
    }

    private void listenTagsChanges(){
        // TODO
    }

    private void listenUsersChanges(){
        // TODO
    }

    private void listenDisplaysChanges(){
        // Current Displays
        this.projectModel.getDisplays().forEach(this::installLink);
        // Future Displays
        this.projectModel.getDisplays().addListener((ListChangeListener<Display>) c -> {
            while (c.next()) {
                for (Display newDisplay : c.getAddedSubList()) {
                    installLink(newDisplay);
                }
            }
        });
    }

    private void installLink(Display display){
        display.fxNodeProperty().addListener((o1, oldNode, newNode) -> {
            if(newNode != null){
                DynamoExtractor extractor = new DynamoExtractor();
                List<Dynamo> dynamos = extractor.extractFrom(newNode);
                projectModel.getTags().forEach(tag ->
                        dynamos.stream()
                               .filter(dynamo  -> dynamo.getCurrentValueTagID() == tag.getId())
                               .forEach(dynamo -> tag.valueProperty().addListener((obj, old, newValue) -> dynamo.setCurrentTagValue(newValue)))
                );
            }
        });
    }

    private void installLink(Connection connection){
        connection.connectedProperty().addListener((o, wasConnected, isConnected) -> {
            if (isConnected){
                fieldbus.startDirector();
                projectModel.getDevices()
                        .stream()
                        .filter(device -> device instanceof Modbus)
                        .filter(Device::getEnabled)
                        .filter(device -> device.getConnection().equals(connection))
                        .forEach(device -> fieldbus.add(device, new ModbusDeviceReading()));
                projectModel.getTags()
                        .stream()
                        .filter(Tag::getEnabled)
                        .filter(tag -> tag.getLinkedAddress() != null)
                        .filter(tag -> tag.getLinkedAddress().getOwner().getConnection().equals(connection))
                        .forEach(tagsHandler::add);
            } else {
                projectModel.getDevices()
                        .stream()
                        .filter(device -> device instanceof Modbus)
                        .filter(Device::getEnabled)
                        .filter(device -> device.getConnection().equals(connection))
                        .forEach(fieldbus::remove);
                projectModel.getTags()
                        .stream()
                        .filter(Tag::getEnabled)
                        .filter(tag -> tag.getLinkedAddress() != null)
                        .filter(tag -> tag.getLinkedAddress().getOwner().getConnection().equals(connection))
                        .forEach(tagsHandler::remove);
            }
        });
    }
}


