import { PropsWithChildren, useEffect, useState } from "react";
import useQuery from "../hooks/useQuery";

import { BE_ENDPOINT } from "../config/api";
import { Navigate } from "react-router-dom";

const ActiveGameSessionFinderLayout = ({ children }: PropsWithChildren) => {

  const [activeGameId, setActiveGameId] = useState<string>();
  const { get } = useQuery();
  const { execute: findSession } = get({
    url: `${BE_ENDPOINT}/user`,
    responseHandler: ({ gameId }) => gameId && setActiveGameId(gameId),
  })

  useEffect(findSession, []);

  return (
    activeGameId
      ? <Navigate to={'/game'}/>
      : <div>{children}</div>
  );
}

export default ActiveGameSessionFinderLayout;
