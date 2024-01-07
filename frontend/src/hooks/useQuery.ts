import { useMessageContext } from "../context/MessageProvider";
import { useState } from "react";
import { getLoggedInUserId } from "../utils/auth";

const USER_ID_KEY_HEADER = 'user_id';

type QueryMethod = 'GET' | 'POST' | 'PUT';

interface QueryProps {
  url: string;
  onSuccess?: () => void;
  responseHandler?: (data: any) => void;
}

const fetchParams = (method: string, body?: any): RequestInit => {
  const defaultParams: RequestInit = {
    method: method,
    mode: 'cors',
    credentials: 'include',
    headers: {
      [USER_ID_KEY_HEADER]: getLoggedInUserId(),
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
  const [queryStatuses, setQueryStatuses] = useState<Record<string, boolean>>({});

  const showDefaultError = () => {
    showError('Error occurred. Please try again or reload the page');
  }

  const handleError = (error: Error) => {
    console.error(`http request error: ${error.message}`)
    showDefaultError();
  };

  const get = (queryProps: QueryProps) => {
    const { execute, isLoading } = setUpQuery('GET', queryProps);
    return {
      execute: () => execute(),
      isLoading,
    }
  }

  const put = (queryProps: QueryProps) => {
    return setUpQuery('PUT', queryProps);
  }

  const post = (queryProps: QueryProps) => {
    return setUpQuery('POST', queryProps);
  }

  const setUpQuery = (method: QueryMethod, { url, onSuccess, responseHandler }: QueryProps) => {
    const key = `${method}:${url}`;
    const execute = (body?: any) => {
      setQueryStatuses(prevState => ({
        ...prevState,
        [key]: true,
      }));
      fetch(url, fetchParams(method, body))
        .then(response => processResponse(response, onSuccess, responseHandler))
        .catch(handleError)
        .finally(() => setQueryStatuses(prevState => ({
          ...prevState,
          [key]: false,
        })));
    }
    const isLoading = queryStatuses[key] || false;

    return { execute, isLoading };
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
    put,
    post,
  };
}

export default useQuery;
