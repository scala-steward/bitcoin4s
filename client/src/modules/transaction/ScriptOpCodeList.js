import _ from 'lodash';
import React from 'react';
import {findElementType} from './ScriptElements';


const ScriptOpCodeList = ({opCodes}) => {
  return (
    <div className='ScriptOpCodeList'>
      {
        _(opCodes)
          .filter((opCode) => opCode.type !== 'OP_PUSHDATA')
          .map((scriptElement, index) => {
            const elementType = findElementType(scriptElement.type);
            const className = `OpCode ${elementType}`

            return (
              <div className={ className } key={index}>
                { _.includes(['ScriptConstant', 'ScriptNum'], scriptElement.type) ? scriptElement.value : scriptElement.type }
              </div>
            );
          }).value()
      }
    </div>
  )
};

export default ScriptOpCodeList;