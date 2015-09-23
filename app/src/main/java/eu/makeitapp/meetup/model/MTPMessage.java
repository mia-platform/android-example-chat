package eu.makeitapp.meetup.model;

import eu.makeitapp.mkbaas.core.MKObject;
import eu.makeitapp.mkbaas.core.annotation.MKCollectionAnnotation;
import eu.makeitapp.mkbaas.core.annotation.MKFieldAnnotation;

//Annotate the name of the collection
@MKCollectionAnnotation(collectionName = "chatmessage")

//MKObject is the standard object and already implements the system collection fields (createdAt, updatedAt..."
public class MTPMessage extends MKObject {

    //Annotate the remote name of fields
    @MKFieldAnnotation(fieldName = "message")
    private String messageText;

    @MKFieldAnnotation(fieldName = "alias")
    private String alias;

    @MKFieldAnnotation(fieldName = "messageAttachment")
    private String messageAttachment;

    public String getMessageText() {
        return messageText;
    }

    public String getAlias() {
        return alias;
    }

    public String getMessageAttachment() {
        return messageAttachment;
    }
}