import React from "react";
import SimpleRangeIndicator from "../../../../widgets/charts/SimpleRangeIndicator";

function CpuLoadIndicator(props) {

  return <SimpleRangeIndicator value={Math.round(props.load)} range={[0, 100]} suffix="%" />

}

export default CpuLoadIndicator
