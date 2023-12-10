import { useMessageContext } from "../context/MessageProvider";
import { useState } from "react";
import { getLoggedInUserId, PLAYER_ID_KEY } from "../utils/auth";

interface IQueryProps {
  method: 'GET' | 'POST';
  url: string;
  body?: any;
  onSuccess?: () => void;
  responseHandler?: (data: any) => void;
}

interface GetQueryProps {
  url: string;
  onSuccess?: () => void;
  responseHandler?: (data: any) => void;
}

interface PostQueryProps {
  url: string;
  body?: any;
  onSuccess?: () => void;
  responseHandler?: (data: any) => void;
}

const fetchParams = (method: string, body?: any): RequestInit => {
  const defaultParams: RequestInit = {
    method: method,
    mode: 'cors',
    credentials: 'include',
    headers: {
      [PLAYER_ID_KEY]: getLoggedInUserId(),
    }
  };
  return body
    ? {
      ...defaultParams,
      headers: {
        ...defaultParams.headers,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body)
    }
    : defaultParams;
}

const useQuery = () => {

  const { showError, showWarning } = useMessageContext();
  const [isLoading, setIsLoading] = useState(false);

  const showDefaultError = () => {
    showError('Error occurred. Please try again or reload the page');
  }

  const handleError = (error: Error) => {
    console.error(`http request error: ${error.message}`)
    showDefaultError();
  };

  const get = ({url, onSuccess, responseHandler}: GetQueryProps) => {
    runQuery({
      method: 'GET',
      url,
      onSuccess,
      responseHandler,
    });
  }

  const post = ({url, body, onSuccess, responseHandler}: PostQueryProps) => {
    runQuery({
      method: 'POST',
      body,
      url,
      onSuccess,
      responseHandler,
    });
  }

  const runQuery = ({method, url, body, onSuccess, responseHandler}: IQueryProps) => {
    setIsLoading(true);
    fetch(url, fetchParams(method, body))
      .then(response => processResponse(response, onSuccess, responseHandler))
      .catch(handleError)
      .finally(() => setIsLoading(false));
  }

  const processResponse = (response: Response, onSuccess?: () => void, responseHandler?: (response: any) => void) => {
    const status = response.status;
    const isJson = response.headers.get('Content-Type') === 'application/json';
    if (status >= 200 && status < 300) {
      onSuccess && onSuccess();
      isJson && responseHandler && response.json()
        .then(response => responseHandler((response)));
    } else if (status >= 400 && status < 500 && isJson) {
      response.json().then(json => {
        showWarning(json.message);
      });
    } else if (status >= 500 && isJson) {
      response.json().then(json => {
        showDefaultError();
        console.error(json.message);
      })
    } else {
      showDefaultError();
      throw Error('Unexpected response status / response content');
    }
  }

  return {
    get,
    post,
    isLoading,
  };
}

export default useQuery;
