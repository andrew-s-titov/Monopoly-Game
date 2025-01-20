import { useDispatch, useSelector } from "react-redux";

import { RootState, AppDispatch } from "../store";

import useModal from "./useModal";
import usePropertyActions from "./usePropertyActions";
import useQuery from "./useQuery";
import useWebsocket from "./useWebsocket";

const useStateSelector = useSelector.withTypes<RootState>();
const useStateDispatch = useDispatch.withTypes<AppDispatch>();

export {
  useModal,
  usePropertyActions,
  useQuery,
  useWebsocket,
  useStateSelector,
  useStateDispatch,
}
