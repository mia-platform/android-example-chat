package eu.makeitapp.meetup.model;

import eu.makeitapp.mkbaas.MKObject;
import eu.makeitapp.mkbaas.annotation.MKCollectionAnnotation;
import eu.makeitapp.mkbaas.annotation.MKFieldAnnotation;

//Annotate the name of the collection
@MKCollectionAnnotation(collectionName = "DroidconMessages")

//MKObject is the standard object and already implements the system collection fields (createdAt, updatedAt..."
public class MTPMessage extends MKObject {

    //Annotate the remote name of fields
    @MKFieldAnnotation(fieldName = "messageText")
    private String messageText;

    @MKFieldAnnotation(fieldName = "messageCreatorName")
    private String messageCreatorName;

    @MKFieldAnnotation(fieldName = "messageAttachment")
    private String messageAttachment;

    public String getMessageText() {
        return messageText;
    }

    public String getMessageCreatorName() {
        return messageCreatorName;
    }

    public String getMessageAttachment() {
        return messageAttachment;
    }
}