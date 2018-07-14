package olive.walkinggroup.dataobjects;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

/**
 * Helper class for Message class.
 * Contains methods that are used in Message related activities.
 */

public class MessageHelper {

    // PRECOND: messageList contains messages for one user only
    // Note: this uses a slow sorting algorithm.
    public static List<List<Message>> groupByContact(List<Message> messageList) {
        List<List<Message>> myMessagesListOfList = new ArrayList<>();

        for (int i = 0; i < messageList.size(); i++) {
            Message currentMessage = messageList.get(i);
            addToMyMessagesList(currentMessage, myMessagesListOfList);
        }

        return myMessagesListOfList;
    }

    public static void addToMyMessagesList(Message message, List<List<Message>> myMessagesListOfList) {
        long messageContactId = getMessageContactId(message);

        // Search for List<Messages> associated with contact in myMessagesList
        for (int i = 0; i < myMessagesListOfList.size(); i++) {
            List<Message> currentList = myMessagesListOfList.get(i);
            Message currentListHead = currentList.get(0);

            if (MessageHelper.getMessageContactId(currentListHead) == messageContactId) {
                currentList.add(message);
                return;
            }
        }

        // Contact not yet exist. Create new list with contact Messages
        List<Message> newList = new ArrayList<>();
        newList.add(message);
        myMessagesListOfList.add(newList);
    }

    // When displaying sent messages also:
    // The contact can be either the sender or the receiver, and is not currentUser.
    // If the message is not related to currentUser (currentUser is not sender nor receiver),
    // the contact is returned as null
    public static User getMessageContact(Message message) {
        User currentUser = Model.getInstance().getCurrentUser();

        if (Objects.equals(message.getToUser().getId(), currentUser.getId())) {
            // If currentUser is receiver
            return message.getFromUser();
        } else if (Objects.equals(message.getFromUser().getId(), currentUser.getId())){
            // If currentUser is sender
            return message.getToUser();
        } else {
            // Message is not related to currentUser
            return null;
        }
    }

    // Return the id of the contact associated with message.
    // Return -1 if message is not related to currentUser.
    public static long getMessageContactId(Message message) {
        User contact = getMessageContact(message);

        if (contact == null) {
            return -1;
        }
        return contact.getId();
    }

    // Sort message list in chronological order, latest on top
    public static List<Message> sortMessageList(List<Message> listToSort) {
        Collections.sort(listToSort, new MessageComparator());
        return listToSort;
    }

    private static class MessageComparator implements Comparator<Message> {
        @Override
        public int compare(Message o1, Message o2) {
            Date time1 = o1.getTimestamp();
            Date time2 = o2.getTimestamp();

            if (time1 != null && time2 != null) {
                return time2.compareTo(time1);
            }

            return 0;
        }
    }

    // Sort list of list of message in chronological order of latest message on each list
    public static List<List<Message>> sortMessageListOfList(List<List<Message>> listToSort) {
        for (int i = 0; i < listToSort.size(); i++) {
            List<Message> currentList = sortMessageList(listToSort.get(i));
            listToSort.set(i, currentList);
        }

        Collections.sort(listToSort, new MessageListComparator());
        return listToSort;
    }

    private static class MessageListComparator implements Comparator<List<Message>> {
        @Override
        public int compare(List<Message> o1, List<Message> o2) {
            Date time1 = o1.get(0).getTimestamp();
            Date time2 = o2.get(0).getTimestamp();

            if (time1 != null && time2 != null) {
                return time2.compareTo(time1);
            }

            return 0;
        }
    }

    public static List<String> parseMessageText(String messageText) {
        List<String> returnList = new ArrayList<>();

        String headerTag = "<header>";
        int headerTagSize = headerTag.length();

        int firstIndex = messageText.indexOf(headerTag);
        int lastIndex = messageText.lastIndexOf(headerTag);

        String header = messageText.substring((firstIndex + headerTagSize), lastIndex);
        returnList.add(header);
        String body = messageText.substring((lastIndex + headerTagSize));
        returnList.add(body);

        return returnList;
    }



    public static List<Message> makeTestList() {
        Model model = Model.getInstance();
        User currentUser = model.getCurrentUser();

        List<Message> returnList = new ArrayList<>();

        returnList.add(makeTestMessage(101, 5, "Hello", makeUser(501, "Bobby C."), currentUser));
        returnList.add(makeTestMessage(102, 11, "One ring to rule them ALL", makeUser(502, "Dark Lord Sauron"), currentUser));
        returnList.add(makeTestMessage(103, 9, "Buy me some stuff, here's a list:\n-milk\n-eggs\n-goo", makeUser(501, "Bobby C."), currentUser));
        returnList.add(makeTestMessage(104, 8, "SECRET!!!", makeUser(401, "Hidden"), makeUser(402, "Hidden")));
        returnList.add(makeTestMessage(105, 12, "Fun Fair!", currentUser, makeUser(505, "Ronald R.")));

        return returnList;
    }

    private static Message makeTestMessage(int id, int timeInc, String text, User fromUser, User toUser) {
        Message message = new Message();
        message.setId((long) id);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, timeInc);
        Date date = cal.getTime();
        message.setTimestamp(date);
        message.setText(text);
        message.setFromUser(fromUser);
        message.setToUser(toUser);
        return message;
    }

    private static User makeUser(int id, String name) {
        User user = new User();
        user.setId((long) id);
        user.setName(name);
        return user;
    }
}
