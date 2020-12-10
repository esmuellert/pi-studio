import React, { useState, useEffect } from "react";
import Link from "@material-ui/core/Link";
import { makeStyles } from "@material-ui/core/styles";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Title from "../common/Title";
import axios from "axios";
import { url, formatFullTime, convertStatus } from "../common/utils";
import MenuItem from "@material-ui/core/MenuItem";
import FormControl from "@material-ui/core/FormControl";
import Select from "@material-ui/core/Select";
import Input from "@material-ui/core/Input";
import ChatIcon from "@material-ui/icons/Chat";
import DoneIcon from "@material-ui/icons/Done";
import { Button } from "@material-ui/core";
import PhotoCameraIcon from "@material-ui/icons/PhotoCamera";
import ImageIcon from "@material-ui/icons/Image";
import PublishIcon from "@material-ui/icons/Publish";
import PlayArrowIcon from "@material-ui/icons/PlayArrow";
import S3 from "react-aws-s3";
// Generate Order Data

function preventDefault(event) {
  event.preventDefault();
}

const useStyles = makeStyles((theme) => ({
  seeMore: {
    marginTop: theme.spacing(3),
  },
  icon: { display: "none" },
}));

export default function OrdersList(props) {
  const classes = useStyles();
  const [rows, setRows] = useState([]);
  const [schedule, setSchedule] = useState({});
  useEffect(() => {
    let ignore = false;
    async function request() {
      await axios
        .get(`${url}/order`, {
          headers: {
            Authorization: localStorage.getItem("token"),
          },
        })
        .then((response) => {
          if (!ignore) {
            let schedules = {};
            response.data.forEach((element) => {
              schedules[element.orderNumber] = element.schedule[0];
            });
            setSchedule(schedules);
            setRows(response.data);
            console.log(response.data);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    }
    request();
    return () => {
      ignore = true;
    };
  }, []);

  const handleScheduleChange = (event) => {
    let schedules = Object.assign({}, schedule);
    schedules[event.currentTarget.id] = event.target.value;
    setSchedule(schedules);
  };

  const handleOtherStatus = (event) => {
    let orderNumber = event.currentTarget.id;
    axios
      .patch(
        `${url}/order/${orderNumber}`,
        {},
        { headers: { Authorization: localStorage.getItem("token") } }
      )
      .then((response) => {
        let newRows = rows.map((x) => x);
        newRows.forEach((element) => {
          if (element.orderNumber === response.data.orderNumber) {
            switch (element.orderStatus) {
              case "RECEIVED":
                element.orderStatus = "IMAGING";
                break;
              case "IMAGING":
                element.orderStatus = "PROCESSING";
                break;
              case "PROCESSING":
                element.orderStatus = "FINISHED";
                break;
              default:
                break;
            }
          }
        });
        setRows(newRows);
      })
      .catch((error) => {
        alert("操作失败，请重试！");
      });
  };

  const handleStatusPlaced = (event) => {
    let orderNumber = event.currentTarget.id;
    axios
      .patch(
        `${url}/order/${orderNumber}`,
        { schedule: schedule[orderNumber] },
        {
          headers: {
            Authorization: localStorage.getItem("token"),
          },
        }
      )
      .then((response) => {
        let newRows = rows.map((x) => x);
        newRows.forEach((element) => {
          if (element.orderNumber === response.data.orderNumber) {
            element.orderStatus = "RECEIVED";
            element.schedule = [schedule[response.data.orderNumber]];
          }
        });
        setRows(newRows);
      })
      .catch((error) => {
        console.log(error);
        alert("操作失败，请重试！");
      });
  };

  const handleUploadImage = (event) => {
    if (event.target.files && event.target.files[0]) {
      const image = event.target.files[0];
      const config = {
        bucketName: "pi-studio",
        dirName: "image" /* optional */,
        region: "ap-east-1",
        accessKeyId: "AKIARGCHDWR4NJBRQHF5",
        secretAccessKey: "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",
      };
      const reactS3Client = new S3(config);
      reactS3Client
        .uploadFile(image, "testfile.jpg")
        .then((data) => console.log(data))
        .catch((error) => console.error(error));
    }
  };

  return (
    <React.Fragment>
      <Title>Recent Orders</Title>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell align="center">订单号</TableCell>
            <TableCell align="center">下单时间</TableCell>
            <TableCell align="center">订单状态</TableCell>
            <TableCell align="center">微信号</TableCell>
            <TableCell align="center">电话</TableCell>
            <TableCell align="center">拍摄类型</TableCell>
            <TableCell align="center">预约时间</TableCell>
            <TableCell align="center">操作</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.orderNumber}>
              <TableCell align="center">{row.orderNumber}</TableCell>
              <TableCell align="center">
                {formatFullTime(row.orderedTime)}
              </TableCell>
              <TableCell align="center">
                {convertStatus(row.orderStatus)}
              </TableCell>
              <TableCell align="center">{row.wechatId}</TableCell>
              <TableCell align="center">{row.phoneNumber}</TableCell>
              <TableCell align="center">{row.type}</TableCell>

              {(() => {
                if (row.schedule.length > 1 || row.orderStatus === "PLACED") {
                  return (
                    <TableCell align="center">
                      <FormControl>
                        <Select
                          value={schedule[row.orderNumber]}
                          onChange={handleScheduleChange}
                        >
                          {row.schedule.map((schedule) => (
                            <MenuItem
                              id={row.orderNumber}
                              key={schedule}
                              value={schedule}
                            >
                              {formatFullTime(schedule)}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>{" "}
                    </TableCell>
                  );
                } else {
                  return (
                    <TableCell align="center">
                      <Select
                        input={<Input disableUnderline />}
                        value={formatFullTime(row.schedule[0])}
                        classes={{
                          icon: classes.icon,
                        }}
                      >
                        <MenuItem value={formatFullTime(row.schedule[0])}>
                          {formatFullTime(row.schedule[0])}
                        </MenuItem>
                      </Select>
                    </TableCell>
                  );
                }
              })()}

              <TableCell align="center">
                {" "}
                {(() => {
                  switch (row.orderStatus) {
                    case "PLACED":
                      return (
                        <Button
                          onClick={handleStatusPlaced}
                          id={row.orderNumber}
                        >
                          <PlayArrowIcon color="primary" />
                        </Button>
                      );
                    case "RECEIVED":
                      return (
                        <Button
                          onClick={handleOtherStatus}
                          id={row.orderNumber}
                        >
                          <PhotoCameraIcon color="primary" />
                        </Button>
                      );
                    case "IMAGING":
                      return (
                        <Button
                          onClick={handleOtherStatus}
                          id={row.orderNumber}
                        >
                          <ImageIcon color="primary" />
                        </Button>
                      );
                    case "PROCESSING":
                      return (
                        <Button
                          onClick={handleOtherStatus}
                          id={row.orderNumber}
                        >
                          <DoneIcon color="primary" />
                        </Button>
                      );
                    default:
                      return null;
                  }
                })()}{" "}
                {row.orderStatus === "PROCESSING" ? (
                  <Button component="label">
                    <input type="file" hidden onChange={handleUploadImage} />
                    <PublishIcon color="primary"></PublishIcon>
                  </Button>
                ) : null}
                <Button onClick={props.onClickChatIcon} id={row.orderNumber}>
                  <ChatIcon color="primary" />
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <div className={classes.seeMore}>
        <Link color="primary" href="#" onClick={preventDefault}>
          See more orders
        </Link>
      </div>
    </React.Fragment>
  );
}
