import React from 'react';
import Reflux from 'reflux';

class Footer extends Reflux.Component {

    render() {
        return (
            <div className="row" id="footer">
                <div className="col-md-12 mt-lg-3">
                    <div className="text-center text-dark">
                        <p>
                            Made in Texas and Europe by <a href="https://go.nzyme.org/lennart" target="_blank">@_lennart </a>
                            and all <a href="https://go.nzyme.org/contributors" target="_blank">contributors</a>.
                        </p>

                        <p>
                            pugnantis latus defensantes
                        </p>
                    </div>
                </div>
            </div>
        )
    }

}

export default Footer;