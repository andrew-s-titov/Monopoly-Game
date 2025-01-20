import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { ChatMessageBody, SystemMessageBody } from "../../types/interfaces";

export type ChatMessage = ChatMessageBody | SystemMessageBody;
interface ChatHistoryState {
  messages: ChatMessage[],
}

const initialState: ChatHistoryState = {
  messages: [],
};

export const chatHistorySlice = createSlice({
  name: 'chatHistory',
  initialState,
  reducers: {
    addToChatHistory: (chatHistoryState, action: PayloadAction<ChatMessage>) => {
      chatHistoryState.messages = [...chatHistoryState.messages, action.payload];
    },
  },
});

export const { addToChatHistory } = chatHistorySlice.actions;

export default chatHistorySlice.reducer;
