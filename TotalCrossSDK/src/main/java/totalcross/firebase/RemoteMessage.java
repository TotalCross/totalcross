// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.firebase;

import totalcross.notification.Notification;

import java.util.HashMap;

public class RemoteMessage {
    private HashMap<String, String> data;
    private String collapsedKey;
    private String messageId;
    private String messageType;
    private int ttl;

    public RemoteMessage () {
        data = new HashMap<String, String>();
    }

    public static final class Builder {
        RemoteMessage rm;

        public Builder () {
            rm = new RemoteMessage();
        }

        public Builder addData(String key, String value) {
            rm.data.put(key, value);
            return this;
        }

        public RemoteMessage build() {
            return rm;
        }

        public Builder clearData() {
            rm = new RemoteMessage();
            return this;
        }

        public Builder setCollapsedKey(String collapsedKey) {
            rm.collapsedKey = collapsedKey;
            return this;
        }

        public Builder setData(HashMap<String, String> data) {
            rm.data = data;
            return this;
        }

        public Builder setMessageId(String messageId) {
            rm.messageId = messageId;
            return this;
        }

        public Builder setMessageType(String messageType) {
            rm.messageType = messageType;
            return this;
        }

        public Builder setTtl(int ttl) {
            rm.ttl = ttl;
            return this;
        }
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public String getCollapsedKey() {
        return collapsedKey;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public int getTtl() {
        return ttl;
    }
}
